package org.trolie.client.request.streaming;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trolie.client.request.streaming.exception.StreamingGetConnectionException;
import org.trolie.client.request.streaming.exception.StreamingGetResponseException;
import org.trolie.client.request.streaming.exception.SubscriberInternalException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base for a polling subscriber to a GET endpoint with conditional GET semantics and compressed response body.
 * 
 * @param <T>
 */
public abstract class AbstractStreamingGet<T extends StreamingResponseReceiver> {

	Logger logger = LoggerFactory.getLogger(AbstractStreamingGet.class);
	
	HttpClient httpClient;
	HttpHost host;
	RequestConfig requestConfig;
	int bufferSize;
	ThreadPoolExecutor threadPoolExecutor;
	protected ObjectMapper objectMapper;
	protected T receiver;

	Future<Void> requestExecutorFuture;
	
	protected abstract String getPath();
	protected abstract String getContentType();
	
	/**
	 * Handle new content. This method should not throw exceptions but rather report them to {@link StreamingSubscribedResponseReceiver#error(org.trolie.client.request.streaming.exception.StreamingGetException)}
	 * 
	 * @param inputStream
	 */
	protected abstract void handleResponseContent(InputStream inputStream);
	
	protected AbstractStreamingGet(
			HttpClient httpClient, 
			HttpHost host, 
			RequestConfig requestConfig,
			int bufferSize, 
			ObjectMapper objectMapper,
			T receiver) {
		super();
		this.httpClient = httpClient;
		this.host = host;
		this.requestConfig = requestConfig;
		this.bufferSize = bufferSize;
		this.objectMapper = objectMapper;
		this.receiver = receiver;
		this.threadPoolExecutor = new ThreadPoolExecutor(2,2,10,
				TimeUnit.SECONDS, new LinkedBlockingDeque<>());
	}
	
	protected HttpClientResponseHandler<Void> createResponseHandler() {
		return response -> {
			handleResponse(response);
			return null;
		};
	}

	protected void handleResponse(ClassicHttpResponse response) {
		if (response.getCode() == HttpStatus.SC_OK) {
			//create a new thread to consume the response stream to 
			//allow for a buffer between HTTP I/O and whatever is handling the data
			try {
				threadPoolExecutor.submit(new HandlerExecutor(response.getEntity().getContent())).get();
			} catch (IOException e) {
				logger.error("I/O error initiating request",e);
				receiver.error(new StreamingGetConnectionException(e));
			} catch (Exception e) {
				logger.error("Internal error handling response",e);
				receiver.error(new SubscriberInternalException(e));
			}
		} else if (response.getCode() == HttpStatus.SC_NOT_MODIFIED) {
			logger.trace("Server responded with status code 304. The requested resource has not changed.");
		} else {
			String s = "Server responded with status code " + response.getCode();
			logger.error(s);
			receiver.error(new StreamingGetResponseException(s, response.getCode()));
		}
	}
	
	protected HttpGet createRequest() throws URISyntaxException {
		HttpGet get = new HttpGet(getPath());
		get.addHeader(HttpHeaders.ACCEPT, getContentType());
		
		get.setConfig(requestConfig);
		return get;
	}
	
	public void executeRequest() {
		try {
			
			HttpGet get = createRequest();
			httpClient.execute(host, get, createResponseHandler());
		
		} catch (IOException e) {
			logger.error("I/O error initiating request",e);
			receiver.error(new StreamingGetConnectionException(e));
		} catch (Exception e) {
			receiver.error(new SubscriberInternalException(e));
		}
	}
	
	private class HandlerExecutor implements Callable<Void> {

		InputStream inputStream;
		
		public HandlerExecutor(InputStream inputStream) {
			super();
			this.inputStream = inputStream;
		}

		@Override
		public Void call() throws Exception {
			try (BufferedInputStream bufferedIn = new BufferedInputStream(inputStream, bufferSize)) {
				handleResponseContent(bufferedIn);
			} catch (IOException e) {
				receiver.error(new StreamingGetConnectionException(e));
			} catch (Exception e) {
				receiver.error(new SubscriberInternalException(e));
			}
			return null;
		}
		
	}
	
}
