package org.trolie.client.impl.request.operatingsnapshots;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.trolie.client.TrolieApiConstants;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotReceiver;

import java.time.Instant;
import java.util.Map;

/**
 * On-demand GET request for regional forecast limits with no ETAG usage
 */
public class RegionalForecastSnapshotRequest extends ForecastSnapshotRequest {

	public RegionalForecastSnapshotRequest(
			HttpClient httpClient, 
			HttpHost host, 
			RequestConfig requestConfig,
			int bufferSize, 
			ObjectMapper objectMapper,
			Map<String, String> httpHeaders,
			ForecastSnapshotReceiver receiver,
			String monitoringSet,
			String resourceId,
			Instant offsetPeriodStart,
			Instant periodEnd) {
		
		super(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders, receiver,
				monitoringSet, resourceId, offsetPeriodStart, periodEnd);
	}

	@Override
	protected String getPath() {
		return TrolieApiConstants.PATH_REGIONAL_FORECAST_SNAPSHOT;
	}
	
}
