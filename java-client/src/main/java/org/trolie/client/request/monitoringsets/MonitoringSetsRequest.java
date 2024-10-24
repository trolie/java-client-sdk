package org.trolie.client.request.monitoringsets;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.net.URIBuilder;
import org.trolie.client.request.streaming.AbstractStreamingGet;
import org.trolie.client.util.TrolieApiConstants;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * On-demand GET request for forecast limits with no ETAG usage
 */
public class MonitoringSetsRequest extends AbstractStreamingGet<MonitoringSetsReceiver> {

	JsonFactory jsonFactory;
	String monitoringSet;
	
	public MonitoringSetsRequest(
			HttpClient httpClient, 
			HttpHost host, 
			RequestConfig requestConfig,
			int bufferSize, 
			ThreadPoolExecutor threadPoolExecutor, 
			ObjectMapper objectMapper, 
			MonitoringSetsReceiver receiver,
			String monitoringSet) {
		
		super(httpClient, host, requestConfig, bufferSize, objectMapper, receiver);
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
			//TODO: throw exception
		}
		return get;
	}

	@Override
	protected void handleResponseContent(InputStream inputStream) {
		new MonitoringSetsResponseParser(receiver).parseResponse(inputStream, jsonFactory);
	}

	
}
