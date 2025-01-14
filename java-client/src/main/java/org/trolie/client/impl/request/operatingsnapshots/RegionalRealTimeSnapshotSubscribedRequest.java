package org.trolie.client.impl.request.operatingsnapshots;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.trolie.client.ETagStore;
import org.trolie.client.TrolieApiConstants;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotSubscribedReceiver;

import java.util.Map;

/**
 * Subscribed request for regional real-time rating snapshots
 */
public class RegionalRealTimeSnapshotSubscribedRequest extends RealTimeSnapshotSubscribedRequest {

    public RegionalRealTimeSnapshotSubscribedRequest(
            HttpClient httpClient,
            HttpHost host,
            RequestConfig requestConfig,
            int bufferSize,
            ObjectMapper objectMapper,
            Map<String, String> httpHeaders,
            int pollingRateMillis,
            RealTimeSnapshotSubscribedReceiver receiver,
            ETagStore eTagStore,
            String monitoringSet) {

        super(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders, pollingRateMillis, receiver,
                eTagStore, monitoringSet, null);
    }

    @Override
    protected String getPath() {
        return TrolieApiConstants.PATH_REGIONAL_REALTIME_SNAPSHOT;
    }

}
