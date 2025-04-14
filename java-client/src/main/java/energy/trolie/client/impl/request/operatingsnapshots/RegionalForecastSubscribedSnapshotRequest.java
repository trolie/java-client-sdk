package energy.trolie.client.impl.request.operatingsnapshots;

import com.fasterxml.jackson.databind.ObjectMapper;
import energy.trolie.client.ETagStore;
import energy.trolie.client.TrolieApiConstants;
import energy.trolie.client.TrolieHost;
import energy.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscribedReceiver;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;

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
            ObjectMapper objectMapper,
            Map<String, String> httpHeaders,
            int pollingRateMillis,
            ForecastSnapshotSubscribedReceiver receiver,
            ETagStore eTagStore,
            String monitoringSet) {

        super(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders, pollingRateMillis, receiver,
                eTagStore, monitoringSet);
    }

    @Override
    protected String getPath() {
        return TrolieApiConstants.PATH_REGIONAL_FORECAST_SNAPSHOT;
    }

}
