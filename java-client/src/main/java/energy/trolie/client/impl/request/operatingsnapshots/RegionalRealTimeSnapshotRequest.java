package energy.trolie.client.impl.request.operatingsnapshots;

import com.fasterxml.jackson.databind.ObjectMapper;
import energy.trolie.client.RequestHeaderProvider;
import energy.trolie.client.TrolieApiConstants;
import energy.trolie.client.TrolieHost;
import energy.trolie.client.request.operatingsnapshots.RealTimeSnapshotReceiver;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;

import java.util.List;
import java.util.Map;

/**
 * On-demand GET request for regional real-time limits with no ETAG usage
 */
public class RegionalRealTimeSnapshotRequest extends RealTimeSnapshotRequest {

	public RegionalRealTimeSnapshotRequest(
			HttpClient httpClient, 
			TrolieHost host,
			RequestConfig requestConfig,
			int bufferSize, 
			ObjectMapper objectMapper,
			Map<String, String> httpHeaders,
			List<RequestHeaderProvider> providers,
			RealTimeSnapshotReceiver receiver,
			String monitoringSet,
			String resourceId) {
		
		super(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders, providers, receiver,
				monitoringSet, resourceId);
	}

	@Override
	protected String getPath() {
		return TrolieApiConstants.PATH_REGIONAL_REALTIME_SNAPSHOT;
	}
	
}
