package energy.trolie.client.impl.request.operatingsnapshots;

import energy.trolie.client.ETagStore;
import energy.trolie.client.RequestHeaderProvider;
import energy.trolie.client.TrolieApiConstants;
import energy.trolie.client.TrolieHost;
import energy.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscribedReceiver;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

/**
 * Subscription for regional forecast rating snapshots
 */
public class RegionalForecastSubscribedSnapshotRequest extends ForecastSnapshotSubscribedRequest {

    public RegionalForecastSubscribedSnapshotRequest(
            HttpClient httpClient,
            TrolieHost host,
            RequestConfig requestConfig,
            int bufferSize,
            JsonMapper jsonMapper,
            Map<String, String> httpHeaders,
            List<RequestHeaderProvider> providers,
            int pollingRateMillis,
            ForecastSnapshotSubscribedReceiver receiver,
            ETagStore eTagStore,
            String monitoringSet) {

        super(httpClient, host, requestConfig, bufferSize, jsonMapper, httpHeaders, providers, pollingRateMillis, receiver,
                eTagStore, monitoringSet);
    }

    @Override
    protected String getPath() {
        return TrolieApiConstants.PATH_REGIONAL_FORECAST_SNAPSHOT;
    }

}
