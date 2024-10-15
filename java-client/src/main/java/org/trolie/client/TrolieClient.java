package org.trolie.client;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotReceiver;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscribedReceiver;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscribedRequest;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotReceiver;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotSubscribedReceiver;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotSubscribedRequest;
import org.trolie.client.request.ratingproposals.ForecastRatingProposalUpdate;
import org.trolie.client.request.ratingproposals.RealTimeRatingProposalUpdate;


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
    
    /**
     * Execute a request for the current forecast limits with a streaming response handler
     * 
     * @param receiver
     * @param monitoringSet
     * @param transmissionFacility
     */
    void getInUseLimitForecasts(
    		ForecastSnapshotReceiver receiver,
    		String monitoringSet);

    /**
     * Create a polling subscription for forecast snapshot data updates
     * 
     * @param receiver Streaming data receiver for snapshot data
     * @param monitoringSet optional filter for monitoring set 
     * @param pollingRateMillis Interval in millis between polling loops
     * @return
     */
    ForecastSnapshotSubscribedRequest subscribeToInUseLimitForecastUpdates(
    		ForecastSnapshotSubscribedReceiver receiver,
    		String monitoringSet,
    		int pollingRateMillis);

    /**
     * Create a forecast proposal update that can stream the update submission to the server
     * 
     * @return
     */
    ForecastRatingProposalUpdate createForecastRatingProposalStreamingUpdate();

    /**
     * Execute a request for the current real-time limits with a streaming response handler
     * 
     * @param receiver
     * @param monitoringSet
     * @param transmissionFacility
     */
    public void getInUseLimits(RealTimeSnapshotReceiver receiver, String monitoringSet, String transmissionFacility);

    /**
     * Create a polling subscription for real-time snapshot data updates
     * 
     * @param receiver Streaming data receiver for snapshot data
     * @param monitoringSet optional filter for monitoring set
     * @param transmissionFacility optional filter for transmission facility
     * @param pollingRateMillis Interval in millis between polling loops
     * @return
     */
    RealTimeSnapshotSubscribedRequest subscribeToInUseLimits(
    		RealTimeSnapshotSubscribedReceiver receiver,
    		String monitoringSet,
    		String transmissionFacility,
    		int pollingRateMillis);

    /**
     * Create a real-time proposal update that can stream the update submission to the server
     * 
     * @return
     */
    RealTimeRatingProposalUpdate createRealTimeRatingProposalStreamingUpdate();

    static TrolieClientBuilder builder(String baseUrl, HttpClientBuilder clientBuilder) {
        return new TrolieClientBuilder(baseUrl, clientBuilder);
    }
}
