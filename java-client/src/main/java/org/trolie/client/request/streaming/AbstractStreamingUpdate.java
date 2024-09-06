package org.trolie.client.request.streaming;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trolie.client.TrolieException;
import org.trolie.client.TrolieServerException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

public abstract class AbstractStreamingUpdate<T> implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(AbstractStreamingUpdate.class);
	
	HttpClient httpClient;
	HttpHost host;
	RequestConfig requestConfig;
	ThreadPoolExecutor threadPoolExecutor;
	int bufferSize;
	protected ObjectMapper objectMapper;

	PipedOutputStream outputStream;
	Future<T> responseFuture;

	public AbstractStreamingUpdate(HttpClient httpClient, HttpHost host, RequestConfig requestConfig,
			ThreadPoolExecutor threadPoolExecutor, int bufferSize, ObjectMapper objectMapper) {
		super();
		this.httpClient = httpClient;
		this.host = host;
		this.requestConfig = requestConfig;
		this.threadPoolExecutor = threadPoolExecutor;
		this.bufferSize = bufferSize;
		this.objectMapper = objectMapper;
	}

	protected abstract ContentType getContentType();
	protected abstract String getPath();
	protected abstract HttpUriRequestBase getRequest();
	protected abstract Function<HttpEntity,T> getResponseHandler();	
	
	/**
	 * make sure that the thread running the request exists and has not died 
	 */
	protected void checkCanWrite() {
		if (responseFuture == null) {
			throw new IllegalStateException("Request has not initiated. Must call createRequestOutputStream() before writing");
		}
		if (responseFuture.isDone()) {
			try {
				responseFuture.get();
				throw new IllegalStateException("Request terminated prematurely without error");
			} catch (InterruptedException e) {
				throw new TrolieException("Request thread interrupted", e);
			} catch (ExecutionException e) {
				throw new TrolieException("Request terminated prematurely with error", e.getCause());
			}
		}
	}

	/**
	 * handle an error writing data
	 */
	protected void handleWriteError(Exception e) {
		//if the exception is a broken pipe chances are the more interesting error is in the request thread
		if (e instanceof IOException && responseFuture != null && responseFuture.isDone()) {
			checkCanWrite();
		}
		close();
		throw new TrolieException("Error writing request data", e);
	}
	
	/**
	 * initiate the request and pipe an output stream to the request entity
	 * 
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected OutputStream createRequestOutputStream() throws IOException,InterruptedException {

		//they probably already set these parameters, but may as well make sure
		HttpUriRequestBase request = getRequest();
		request.addHeader(HttpHeaders.CONTENT_TYPE, getContentType());
		request.setPath(getPath());
		
		//turn on compression
		RequestConfig config = RequestConfig.copy(this.requestConfig)
				.setContentCompressionEnabled(true).build();		
		request.setConfig(config);

		//create a request entity we can write into from a stream
		PipedOutputStream pipedOutputStream = new PipedOutputStream();
		PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream, bufferSize);
		request.setEntity(new InputStreamEntity(pipedInputStream, getContentType()));
		this.outputStream = pipedOutputStream;

		//kick of the thread that will consume our stream and send it to the server
		responseFuture = threadPoolExecutor.submit(new RequestExecutor(request));

		return outputStream;
	}

	/**
	 * Finalize the stream and check for errors on the request thread
	 * 
	 * @return
	 */
	protected T completeRequest() {
		try {
			outputStream.close();
		} catch (IOException e) {
			throw new TrolieException("Error writing request data", e);
		}
		try {
			return responseFuture.get();
		} catch (InterruptedException e) {
			throw new TrolieException("Streaming request thread interrupted",e);
		} catch (ExecutionException e) {
			if (e.getCause() != null && e.getCause() instanceof HttpResponseException) {
				HttpResponseException httpEx = (HttpResponseException)e.getCause();
				throw new TrolieServerException(
						httpEx.getStatusCode(),
						"Trolie server returned error status code " + httpEx.getStatusCode(), 
						e);
			}
			throw new TrolieException("Client error completing request",e); 
		}
	}

	/**
	 * Clean up the stream and terminate the request thread if it is still running
	 */
	@Override
	public void close() {
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				logger.error("Error closing output stream",e);
			}
		}
		if (responseFuture != null && !responseFuture.isDone()) {
			responseFuture.cancel(true);
		}
	}

	@AllArgsConstructor
	private class RequestExecutor implements Callable<T> {

		HttpUriRequestBase request;
		
		@Override
		public T call() throws Exception {
			try {
				return httpClient.execute(host, request, new ResponseHandler());
			} finally {
				//this should already have been closed by the user by this point. If it hasn't, 
				//then we had a request I/O error and the easiest way to bubble that to the
				//other thread is to break the pipe it is writing to. 
				outputStream.close();
			}
		}
	}

	private class ResponseHandler extends AbstractHttpClientResponseHandler<T> {
		@Override
		public T handleEntity(HttpEntity entity) throws IOException {
			return getResponseHandler().apply(entity);
		}
	}
}
