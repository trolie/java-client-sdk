package org.trolie.client.request.operatingsnapshots;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.trolie.client.util.TrolieApiConstants;

import java.util.Map;

/**
 * On-demand GET request for regional real-time limits with no ETAG usage
 */
public class RegionalRealTimeSnapshotRequest extends RealTimeSnapshotRequest {

	public RegionalRealTimeSnapshotRequest(
			HttpClient httpClient, 
			HttpHost host, 
			RequestConfig requestConfig,
			int bufferSize, 
			ObjectMapper objectMapper,
			Map<String, String> httpHeaders,
			RealTimeSnapshotReceiver receiver,
			String monitoringSet,
			String resourceId) {
		
		super(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders, receiver,
				monitoringSet, resourceId);
	}

	@Override
	protected String getPath() {
		return TrolieApiConstants.PATH_REGIONAL_REALTIME_SNAPSHOT;
	}
	
}
