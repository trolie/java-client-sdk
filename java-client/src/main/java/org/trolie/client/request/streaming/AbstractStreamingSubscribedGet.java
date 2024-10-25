package org.trolie.client.request.streaming;

import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trolie.client.etag.ETagStore;
import org.trolie.client.request.streaming.exception.StreamingGetHandlingException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Abstract base for a polling subscriber to a GET endpoint with conditional GET semantics and compressed response body.
 * 
 * @param <T>
 */
public abstract class AbstractStreamingSubscribedGet<T extends StreamingSubscribedResponseReceiver> extends AbstractStreamingGet<T> implements RequestSubscription {

	Logger logger = LoggerFactory.getLogger(AbstractStreamingSubscribedGet.class);
	
	int pollingRateMillis;
	ETagStore eTagStore;

	AtomicBoolean active = new AtomicBoolean();
	
	public AbstractStreamingSubscribedGet(
			HttpClient httpClient, 
			HttpHost host, 
			RequestConfig requestConfig,
			int bufferSize, 
			ObjectMapper objectMapper, 
			int pollingRateMillis, 
			T receiver,
			ETagStore eTagStore) {
		super(httpClient, host, requestConfig, bufferSize, objectMapper, receiver);
		this.pollingRateMillis = pollingRateMillis;
		this.eTagStore = eTagStore;
	}
	
	public void start() {
		if (active.get()) {
			return;
		}
		logger.info("Starting request subscription for {}", getPath());
		active.set(true);
		requestExecutorFuture = threadPoolExecutor.submit(new RequestExecutor());
		receiver.setSubscription(this);
	}
	
	public Future<Void> stop() {
		if (!active.get()) {
			return requestExecutorFuture;
		}
		logger.info("Stopping request subscription for {}", getPath());
		synchronized (active) {
			active.set(false);
			active.notify();
		}
		return requestExecutorFuture;
	}
	
	public boolean isActive() {
		return active.get();
	}

	protected void handleResponse(ClassicHttpResponse response) {
		super.handleResponse(response);
		if (response.getCode() == HttpStatus.SC_OK) {
			try {
				eTagStore.putETag(getPath(), response.getHeader(HttpHeaders.ETAG).getValue());
			} catch (ProtocolException e) {
				logger.error("Error handling server response",e);
				receiver.error(new StreamingGetHandlingException(e));
			}	
		}
	}
	
	protected HttpGet createRequest() throws URISyntaxException {

		HttpGet request = super.createRequest();
		
		//supply our stored ETAG value if we have one
		String etag = eTagStore.getETag(getPath());					
		if (etag != null) {
			request.addHeader(HttpHeaders.IF_NONE_MATCH, etag);
		}

		return request;
	}
	
	private class RequestExecutor implements Callable<Void> {

		@Override
		public Void call() throws Exception {
			
			logger.info("Subscribed to {}", getPath());
			
			while (active.get()) {
				
				logger.debug("Polling for update on {}", getPath());
				executeRequest();
				
				synchronized (active) {
					active.wait(pollingRateMillis);
				}
			}
			
			logger.info("Unsubscribed from {}", getPath());
			
			return null;
		}
		
	}
	
	public boolean isSubscribed() {
		return active.get() && requestExecutorFuture != null && !requestExecutorFuture.isDone();
	}
	
	public String toString() {
		return getPath();
	}
}
