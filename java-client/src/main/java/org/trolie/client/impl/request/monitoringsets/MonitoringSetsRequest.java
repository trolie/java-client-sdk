package org.trolie.client.impl.request.monitoringsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.net.URIBuilder;
import org.trolie.client.TrolieApiConstants;
import org.trolie.client.impl.request.AbstractStreamingGet;
import org.trolie.client.request.monitoringsets.MonitoringSetsReceiver;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * On-demand GET request for forecast limits with no ETAG usage
 */
public class MonitoringSetsRequest extends AbstractStreamingGet<MonitoringSetsReceiver> {

	String monitoringSet;
	
	public MonitoringSetsRequest(
			HttpClient httpClient, 
			HttpHost host, 
			RequestConfig requestConfig,
			int bufferSize, 
			ObjectMapper objectMapper,
			Map<String, String> httpHeaders,
			MonitoringSetsReceiver receiver,
			String monitoringSet) {
		
		super(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders, receiver);

		if (monitoringSet == null || monitoringSet.isBlank()) {
			throw new IllegalArgumentException("Monitoring set name cannot be null or blank");
		}
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
		//add the monitoring set parameter to the base URI
		URIBuilder uriBuilder = new URIBuilder(get.getUri());
		uriBuilder.appendPath(monitoringSet);
		get.setUri(uriBuilder.build());
		return get;
	}

	@Override
	protected Boolean handleResponseContent(InputStream inputStream) {
		return new MonitoringSetsResponseParser(receiver).parseResponse(inputStream, jsonFactory);
	}

	
}
