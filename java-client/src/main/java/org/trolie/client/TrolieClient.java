package org.trolie.client;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotStreamingReceiver;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscription;
import org.trolie.client.request.ratingproposals.ForecastRatingProposalStreamingUpdate;


/**
 * Primary TROLIE client access interface.  Represents configured access to a single
 * TROLIE server.  Should be used as a singleton within applications, at least for each
 * TROLIE endpoint used by a given application.
 * @see TrolieClientBuilder
 */
public interface TrolieClient {

    @FunctionalInterface
    interface ForecastReceiver {
        void accept();
    }
    void getInUseLimitForecasts(String monitoringSet);

    /**
     * Create a polling subscription for forecast snapshot data updates
     * 
     * @param receiver Streaming data receiver for snapshot data
     * @param pollingRateMillis Interval in millis between polling loops
     * @return
     */
    ForecastSnapshotSubscription subscribeToInUseLimitForecastUpdates(
    		ForecastSnapshotStreamingReceiver receiver,
    		String monitoringSet,
    		int pollingRateMillis);

    /**
     * Create a forecast proposal update that can stream the update submission to the server
     * 
     * @return
     */
    ForecastRatingProposalStreamingUpdate createForecastRatingProposalStreamingUpdate();

    void getInUseLimits();

    void subscribeToInUseLimits();

    void updateRealTimeProposal();

    static TrolieClientBuilder builder(String baseUrl, HttpClientBuilder clientBuilder) {
        return new TrolieClientBuilder(baseUrl, clientBuilder);
    }
}
