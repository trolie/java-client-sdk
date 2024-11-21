package org.trolie.client.request.operatingsnapshots;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.net.URIBuilder;
import org.trolie.client.request.streaming.AbstractStreamingGet;
import org.trolie.client.util.TrolieApiConstants;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * On-demand GET request for real-time limits with no ETAG usage
 */
public class RealTimeSnapshotRequest extends AbstractStreamingGet<RealTimeSnapshotReceiver> {

	String monitoringSet;
	String resourceId;
	
	public RealTimeSnapshotRequest(
			HttpClient httpClient, 
			HttpHost host, 
			RequestConfig requestConfig,
			int bufferSize, 
			ObjectMapper objectMapper,
			Map<String, String> httpHeaders,
			RealTimeSnapshotReceiver receiver,
			String monitoringSet,
			String resourceId) {
		
		super(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders, receiver);
		this.monitoringSet = monitoringSet;
		this.resourceId = resourceId;
	}

	@Override
	protected String getPath() {
		return TrolieApiConstants.PATH_REALTIME_SNAPSHOT;
	}
	
	@Override
	protected String getContentType() {
		return TrolieApiConstants.CONTENT_TYPE_REALTIME_SNAPSHOT;
	}
	
	@Override
	protected HttpGet createRequest() throws URISyntaxException {
		
		HttpGet get = super.createRequest();
		
		if (monitoringSet != null) {
		
			//add the monitoring set parameter to the base URI
			URIBuilder uriBuilder = new URIBuilder(get.getUri())
					.addParameter(TrolieApiConstants.PARAM_MONITORING_SET, monitoringSet);
			get.setUri(uriBuilder.build());
		}
		
		if (resourceId != null) {
			
			//add the resource ID parameter to the base URI
			URIBuilder uriBuilder = new URIBuilder(get.getUri())
					.addParameter(TrolieApiConstants.PARAM_RESOURCE_ID, resourceId);
			get.setUri(uriBuilder.build());
		}
		
		return get;
	}

	@Override
	protected Boolean handleResponseContent(InputStream inputStream) {
		return new RealTimeSnapshotResponseParser(receiver).parseResponse(inputStream, jsonFactory);
	}

	
}
