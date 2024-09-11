package org.trolie.client.request.streaming;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.GzipDecompressingEntity;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Abstract base for a polling subscriber to a GET endpoint with conditional GET semantics and compressed response body.
 * 
 * @param <T>
 */
public abstract class AbstractStreamingGetSubscription<T extends SubscriptionUpdateReceiver> implements RequestSubscription {

	Logger logger = LoggerFactory.getLogger(AbstractStreamingGetSubscription.class);
	
	HttpClient httpClient;
	HttpHost host;
	RequestConfig requestConfig;
	int bufferSize;
	ThreadPoolExecutor threadPoolExecutor;
	protected ObjectMapper objectMapper;
	int pollingRateMillis;
	protected T receiver;

	AtomicBoolean subscribed = new AtomicBoolean();
	String etag;
	Future<Void> requestExecutorFuture;
	
	protected abstract String getPath();
	protected abstract String getContentType();
	protected abstract void handleNewContent(InputStream inputStream) throws Exception;
	
	
	public AbstractStreamingGetSubscription(HttpClient httpClient, HttpHost host, RequestConfig requestConfig,
			int bufferSize, ObjectMapper objectMapper, int pollingRateMillis, T receiver) {
		super();
		this.httpClient = httpClient;
		this.host = host;
		this.requestConfig = requestConfig;
		this.bufferSize = bufferSize;
		this.objectMapper = objectMapper;
		this.pollingRateMillis = pollingRateMillis;
		this.receiver = receiver;
		this.threadPoolExecutor = new ThreadPoolExecutor(2,2,pollingRateMillis,TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>());
	}
	public void subscribe() {
		subscribed.set(true);
		requestExecutorFuture = threadPoolExecutor.submit(new RequestExecutor());
		receiver.setSubscription(this);
	}
	
	public Future<Void> unsubscribe() {		
		synchronized (subscribed) {
			subscribed.set(false);
			subscribed.notify();
		}
		return requestExecutorFuture;
	}
	
	protected HttpClientResponseHandler<Void> createResponseHandler() {
		return response -> {
			
			if (response.getCode() == HttpStatus.SC_OK) {
				//if we have new data, update our ETAG value
				etag = response.getHeader(HttpHeaders.ETAG).getValue();
				
				//create a new thread to consume the response stream to 
				//allow for a buffer between HTTP I/O and whatever is handling the data
				try (HttpEntity entity = new GzipDecompressingEntity(response.getEntity())) {
					threadPoolExecutor.submit(new HandlerExecutor(entity.getContent())).get();
				} catch (Exception e) {
					logger.error("Error handling request", e);
					receiver.error(new SubscriberRequestHandlingException(e));
				}
			} else if (response.getCode() != HttpStatus.SC_NOT_MODIFIED) {
				String s = "Server responded with status code " + response.getCode();
				logger.error(s);
				receiver.error(new SubscriberRequestExecutionException(s,response.getCode()));			
			}		
			return null;
		};
	}
	
	private class RequestExecutor implements Callable<Void> {

		@Override
		public Void call() throws Exception {
			
			logger.info("Subscribed to {}", getPath());
			
			while (subscribed.get()) {
				
				try {
				
					logger.debug("Polling for update on {}", getPath());
					
					HttpGet get = new HttpGet(getPath());
					get.addHeader(HttpHeaders.ACCEPT, getContentType());
					
					//will need to revisit this for brotli support
					get.addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip");
					
					//supply our stored ETAG value if we have one
					if (etag != null) {
						get.addHeader(HttpHeaders.IF_NONE_MATCH, etag);
					}

					get.setConfig(requestConfig);
					httpClient.execute(host, get, createResponseHandler());
					
				} catch (Exception e) {
					logger.error("Error executing poll request", e);
					//send the error to the subscription receiver
					receiver.error(new SubscriberRequestExecutionException(e));
				}
				
				synchronized (subscribed) {
					subscribed.wait(pollingRateMillis);
				}
			}
			
			logger.info("Unsubscribed from {}", getPath());
			
			return null;
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
				handleNewContent(bufferedIn);
			} catch (Exception e) {
				logger.error("Error handling update data", e);
			}
			return null;
		}
		
	}
	
	public boolean isSubscribed() {
		return subscribed.get() && requestExecutorFuture != null && !requestExecutorFuture.isDone();
	}
}
