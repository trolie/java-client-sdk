package energy.trolie.client.impl.request;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import energy.trolie.client.StreamingResponseReceiver;
import energy.trolie.client.StreamingSubscribedResponseReceiver;
import energy.trolie.client.TrolieHost;
import energy.trolie.client.exception.StreamingGetConnectionException;
import energy.trolie.client.exception.StreamingGetException;
import energy.trolie.client.exception.StreamingGetResponseException;
import energy.trolie.client.exception.SubscriberInternalException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>Abstract base for all GET requests.</p>
 * <p>API User code should not typically use methods of this class, except in advanced scenarios.</p>
 * 
 * @param <T>
 */
public abstract class AbstractStreamingGet<T extends StreamingResponseReceiver> {

	Logger logger = LoggerFactory.getLogger(AbstractStreamingGet.class);
	
	HttpClient httpClient;
	TrolieHost host;
	RequestConfig requestConfig;
	int bufferSize;
	ThreadPoolExecutor threadPoolExecutor;
	Map<String, String> httpHeaders;

	protected JsonFactory jsonFactory;
	protected T receiver;
	
	protected abstract String getPath();
	protected abstract String getContentType();
	
	/**
	 * Handle new content. This method should not throw exceptions but rather
	 * report them to {@link StreamingSubscribedResponseReceiver#error(StreamingGetException)}
	 * Method should return true if the response content was handled successfully, false if not.
	 * 
	 * @param inputStream uncompressed HTTP response body
	 */
	protected abstract Boolean handleResponseContent(InputStream inputStream);
	
	protected AbstractStreamingGet(
			HttpClient httpClient, 
			TrolieHost host,
			RequestConfig requestConfig,
			int bufferSize, 
			ObjectMapper objectMapper,
			Map<String, String> httpHeaders,
			T receiver) {
		super();
		this.httpClient = httpClient;
		this.host = host;
		this.requestConfig = requestConfig;
		this.bufferSize = bufferSize;
		this.jsonFactory = new JsonFactory(objectMapper);
		this.receiver = receiver;
		this.threadPoolExecutor = new ThreadPoolExecutor(2,2,10,
				TimeUnit.SECONDS, new LinkedBlockingDeque<>());
		this.httpHeaders = httpHeaders;
	}
	
	protected HttpClientResponseHandler<Void> createResponseHandler() {
		return response -> {
			handleResponse(response);
			return null;
		};
	}

	/**
	 * Hand off HTTP response.
	 * @param response response to handle
	 * @return Returns true if the response was handled successfully, or if a 304 was returned.
	 */
	protected boolean handleResponse(ClassicHttpResponse response) {
		if (response.getCode() == HttpStatus.SC_OK) {
			//create a new thread to consume the response stream to 
			//allow for a buffer between HTTP I/O and whatever is handling the data
			try {
				return threadPoolExecutor.submit(new HandlerExecutor(response.getEntity().getContent())).get();
			} catch (IOException e) {
				logger.error("I/O error initiating request",e);
				receiver.error(new StreamingGetConnectionException(e));
			} catch (Exception e) {
				logger.error("Internal error handling response",e);
				receiver.error(new SubscriberInternalException(e));
			}
		} else if (response.getCode() == HttpStatus.SC_NOT_MODIFIED) {
			logger.trace("Server responded with status code 304. The requested resource has not changed.");
			return true;
		} else {
			String s = "Server responded with status code " + response.getCode();
			logger.error(s);
			receiver.error(new StreamingGetResponseException(s, response.getCode()));
		}

		return false;
	}
	
	protected HttpGet createRequest() throws URISyntaxException {
		HttpGet get = new HttpGet(getFullPath());
		get.addHeader(HttpHeaders.ACCEPT, getContentType());
		if (httpHeaders !=  null && !httpHeaders.isEmpty()) {
			httpHeaders.forEach(get::addHeader);
		}
		
		get.setConfig(requestConfig);
		return get;
	}
	
	public void executeRequest() {
		try {
			
			HttpGet get = createRequest();
			httpClient.execute(host.getHost(), get, createResponseHandler());
		
		} catch (IOException e) {
			logger.error("I/O error initiating request",e);
			receiver.error(new StreamingGetConnectionException(e));
		} catch (Exception e) {
			receiver.error(new SubscriberInternalException(e));
		}
	}
	
	private class HandlerExecutor implements Callable<Boolean> {

		InputStream inputStream;
		
		public HandlerExecutor(InputStream inputStream) {
			super();
			this.inputStream = inputStream;
		}

		@Override
		public Boolean call() throws Exception {
			try (BufferedInputStream bufferedIn = new BufferedInputStream(inputStream, bufferSize)) {
				return handleResponseContent(bufferedIn);
			} catch (IOException e) {
				receiver.error(new StreamingGetConnectionException(e));
			} catch (Exception e) {
				receiver.error(new SubscriberInternalException(e));
			}

			return false;
		}
		
	}

	/**
	 * Returns the full path for the current operations. If the TrolieHost includes a base path, it will be included.
	 * @return a String representing the full path of a TROLIE endpoint.
	 */
	protected String getFullPath() {
		return host.hasBasePath() ? host.getBasePath() + getPath() : getPath();
	}
	
}
