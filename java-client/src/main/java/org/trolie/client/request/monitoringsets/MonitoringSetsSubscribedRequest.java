package org.trolie.client.request.monitoringsets;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.net.URIBuilder;
import org.trolie.client.etag.ETagStore;
import org.trolie.client.request.streaming.AbstractStreamingSubscribedGet;
import org.trolie.client.util.TrolieApiConstants;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Subscribed request for monitoring set updates
 */
public class MonitoringSetsSubscribedRequest extends AbstractStreamingSubscribedGet<MonitoringSetsSubscribedReceiver> {

	JsonFactory jsonFactory;
	String monitoringSet;
	
	public MonitoringSetsSubscribedRequest(
			HttpClient httpClient, 
			HttpHost host, 
			RequestConfig requestConfig,
			int bufferSize, 
			ThreadPoolExecutor threadPoolExecutor, 
			ObjectMapper objectMapper, 
			int pollingRateMillis,
			MonitoringSetsSubscribedReceiver receiver,
			ETagStore eTagStore,
			String monitoringSet) {
		
		super(httpClient, host, requestConfig, bufferSize, objectMapper, pollingRateMillis, receiver, eTagStore);
		this.jsonFactory = new JsonFactory(objectMapper);
		this.monitoringSet = monitoringSet;
	}

	@Override
	protected String getPath() {
		return TrolieApiConstants.PATH_MONITORING_SET_ID;
	}
	
	@Override
	protected String getContentType() {
		return TrolieApiConstants.CONTENT_TYPE_MONITORING_SET;
	}

	@Override
	protected HttpGet createRequest() throws URISyntaxException {

		HttpGet get = super.createRequest();
		if (monitoringSet != null && ! monitoringSet.isBlank()) {
			//add the monitoring set parameter to the base URI
			URIBuilder uriBuilder = new URIBuilder(get.getUri());
			uriBuilder.appendPath("/"+monitoringSet);
			get.setUri(uriBuilder.build());
		} else {
			throw new URISyntaxException("", "Monitoring set cannot be null or empty");
		}
		return get;
	}
	
	@Override
	protected Boolean handleResponseContent(InputStream inputStream) {
		
		return new MonitoringSetsResponseParser(receiver).parseResponse(inputStream, jsonFactory);
		
	}
	
}
