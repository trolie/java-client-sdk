package org.trolie.client.request.operatingsnapshots;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.trolie.client.etag.ETagStore;
import org.trolie.client.util.TrolieApiConstants;

import java.util.Map;

/**
 * Subscription for regional forecast rating snapshots
 */
public class RegionalForecastSubscribedSnapshotRequest extends ForecastSnapshotSubscribedRequest {

    public RegionalForecastSubscribedSnapshotRequest(
            HttpClient httpClient,
            HttpHost host,
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
