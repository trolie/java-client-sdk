package energy.trolie.client.impl.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import energy.trolie.client.StreamingUpdate;
import energy.trolie.client.TrolieHost;
import energy.trolie.client.exception.TrolieException;
import energy.trolie.client.exception.TrolieServerException;
import lombok.AllArgsConstructor;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.GzipCompressingEntity;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Base class for streaming updaters
 * @param <T> the raw JSON DTO type associated with the return value from this update.
 */
public abstract class AbstractStreamingUpdate<T> implements StreamingUpdate<T> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractStreamingUpdate.class);
	
	HttpClient httpClient;
	TrolieHost host;
	RequestConfig requestConfig;
	ThreadPoolExecutor threadPoolExecutor;
	int bufferSize;
	protected ObjectMapper objectMapper;
	PipedOutputStream outputStream;
	Future<T> responseFuture;

	Map<String, String> httpHeaders;

	protected AbstractStreamingUpdate(HttpClient httpClient, TrolieHost host, RequestConfig requestConfig,
									  int bufferSize, ObjectMapper objectMapper, Map<String, String> httpHeaders) {
		super();
		this.httpClient = httpClient;
		this.host = host;
		this.requestConfig = requestConfig;
		this.threadPoolExecutor = new ThreadPoolExecutor(1, 1, 1,
				TimeUnit.SECONDS, new LinkedBlockingQueue<>());
		this.bufferSize = bufferSize;
		this.objectMapper = objectMapper;
		this.httpHeaders = httpHeaders;
	}

	/**
	 *
	 * @return content type associated with this update
	 */
	protected abstract ContentType getContentType();

	/**
	 *
	 * @return HTTP path associated with this update
	 */
	protected abstract String getPath();

	/**
	 *
	 * @return Apache HTTP client request associated with this request.
	 */
	protected abstract HttpUriRequestBase getRequest();

	/**
	 *
	 * @return handler associated with this update.
	 */
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
     */
	protected OutputStream createRequestOutputStream() throws IOException {

		//they probably already set these parameters, but may as well make sure
		HttpUriRequestBase request = getRequest();
		request.addHeader(HttpHeaders.CONTENT_TYPE, getContentType());
		if (this.httpHeaders != null && !this.httpHeaders.isEmpty()) {
			httpHeaders.forEach(request::addHeader);
		}
		request.setPath(getFullPath());
		
		request.setConfig(this.requestConfig);

		//create a request entity we can write into from a stream
		PipedOutputStream pipedOutputStream = new PipedOutputStream();
		PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream, bufferSize);
		if (this.requestConfig.isContentCompressionEnabled()) {
			request.setEntity(new GzipCompressingEntity(new InputStreamEntity(pipedInputStream, getContentType())));
		}else {
			request.setEntity(new InputStreamEntity(pipedInputStream, getContentType()));
		}
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
			if (e.getCause() instanceof HttpResponseException httpEx) {
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
				return httpClient.execute(host.getHost(), request, new ResponseHandler());
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

	protected String getFullPath() {
		return host.hasBasePath() ? host.getBasePath() + getPath() : getPath();
	}
}
