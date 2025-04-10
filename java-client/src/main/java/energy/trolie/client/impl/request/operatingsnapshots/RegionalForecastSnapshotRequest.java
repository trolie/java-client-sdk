package energy.trolie.client.impl.request.operatingsnapshots;

import com.fasterxml.jackson.databind.ObjectMapper;
import energy.trolie.client.TrolieApiConstants;
import energy.trolie.client.TrolieHost;
import energy.trolie.client.request.operatingsnapshots.ForecastSnapshotReceiver;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;

import java.time.Instant;
import java.util.Map;

/**
 * On-demand GET request for regional forecast limits with no ETAG usage
 */
public class RegionalForecastSnapshotRequest extends ForecastSnapshotRequest {

	public RegionalForecastSnapshotRequest(
			HttpClient httpClient, 
			TrolieHost host,
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
