package energy.trolie.client.impl.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import energy.trolie.client.ETagStore;
import energy.trolie.client.StreamingSubscribedResponseReceiver;
import energy.trolie.client.TrolieHost;
import energy.trolie.client.exception.StreamingGetHandlingException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>Abstract base for a polling subscriber to a GET endpoint with conditional
 * GET semantics and compressed response body.
 *
 *
 * 
 * @param <T>
 */
public abstract class AbstractStreamingSubscribedGet<T extends StreamingSubscribedResponseReceiver>
		extends AbstractStreamingGet<T>
		implements RequestSubscriptionInternal {

	private static final Logger logger = LoggerFactory.getLogger(AbstractStreamingSubscribedGet.class);
	
	int pollingRateMillis;
	ETagStore eTagStore;

	private final AtomicBoolean active = new AtomicBoolean();

	Future<Void> requestExecutorFuture;
	
	protected AbstractStreamingSubscribedGet(
			HttpClient httpClient, 
			TrolieHost host,
			RequestConfig requestConfig,
			int bufferSize, 
			ObjectMapper objectMapper,
			Map<String, String> httpHeaders,
			int pollingRateMillis,
			T receiver,
			ETagStore eTagStore) {
		super(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders, receiver);
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
			active.notifyAll();
		}
		return requestExecutorFuture;
	}
	
	public boolean isActive() {
		return active.get();
	}

	@Override
	public boolean isHealthy() {
		return !didLastRequestFail();
	}

	@Override
	protected boolean handleResponse(ClassicHttpResponse response) {
		boolean success = super.handleResponse(response);
		// Cache the ETAG if the response was handled successfully and the status is OK
		if (Boolean.TRUE.equals(success) && response.getCode() == HttpStatus.SC_OK) {
			try {
				eTagStore.putETag(getPath(), response.getHeader(HttpHeaders.ETAG).getValue());
			} catch (ProtocolException e) {
				lastRequestFailed = true;
				logger.error("Error handling server response",e);
				receiver.error(new StreamingGetHandlingException(e));
				return false;
			}	
		}

		return success;
	}

	@Override
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

	@Override
	public boolean isSubscribed() {
		return active.get() && requestExecutorFuture != null && !requestExecutorFuture.isDone();
	}
	
	public String toString() {
		return getPath();
	}
}
