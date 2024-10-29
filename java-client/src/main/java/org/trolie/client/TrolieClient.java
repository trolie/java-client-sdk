package org.trolie.client;

import org.apache.hc.client5.http.classic.HttpClient;
import org.trolie.client.request.monitoringsets.DefaultMonitoringSetSubscribedRequest;
import org.trolie.client.request.monitoringsets.MonitoringSetsReceiver;
import org.trolie.client.request.monitoringsets.MonitoringSetsSubscribedReceiver;
import org.trolie.client.request.monitoringsets.MonitoringSetsSubscribedRequest;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotReceiver;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscribedReceiver;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscribedRequest;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotReceiver;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotSubscribedReceiver;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotSubscribedRequest;
import org.trolie.client.request.ratingproposals.ForecastRatingProposalUpdate;
import org.trolie.client.request.ratingproposals.RealTimeRatingProposalUpdate;
import org.trolie.client.request.streaming.RequestSubscription;

import java.io.Closeable;
import java.time.Instant;


/**
 * Primary TROLIE client access interface.  Represents configured access to a single
 * TROLIE server.  Should be used as a singleton within applications, at least for each
 * TROLIE endpoint used by a given application.
 * @see TrolieClientBuilder
 */
public interface TrolieClient extends Closeable {

    /**
     * Execute a request for the given monitoring set.
     *
     * @param receiver
     * @param monitoringSet
     */
    void getMonitoringSet(
    		MonitoringSetsReceiver receiver,
            String monitoringSet);

    /**
     * Create a polling subscription for updates to the given monitoring set.
     *
     * @param receiver
     * @param monitoringSet
     * @param pollingRateMillis
     * @return
     */
    MonitoringSetsSubscribedRequest subscribeToMonitoringSetUpdates(
    		MonitoringSetsSubscribedReceiver receiver,
            String monitoringSet,
    		int pollingRateMillis);

    /**
     * Execute a request for the default monitoring set
     *
     * @param receiver
     */
    void getDefaultMonitoringSet(
    		MonitoringSetsReceiver receiver);

    /**
     * Create a polling subscription for updates to the default monitoring set
     *
     * @param receiver
     * @param pollingRateMillis
     * @return
     */
    DefaultMonitoringSetSubscribedRequest subscribeToDefaultMonitoringSetUpdates(
    		MonitoringSetsSubscribedReceiver receiver,
            int pollingRateMillis);

    /**
     * Execute a request for the current forecast limits with a streaming response handler
     * 
     * @param receiver Streaming data receiver for snapshot data
     */
    void getInUseLimitForecasts(
    		ForecastSnapshotReceiver receiver);
    
    /**
     * Execute a request for the current forecast limits with a streaming response handler
     * 
     * @param receiver Streaming data receiver for snapshot data
     * @param monitoringSet filter for monitoring set name
     */
    void getInUseLimitForecasts(
    		ForecastSnapshotReceiver receiver,
    		String monitoringSet);

    /**
     * Execute a request for the current forecast limits with a streaming response handler
     * 
     * @param receiver Streaming data receiver for snapshot data
     * @param offsetPeriodStart Optional. Only periods starting at or after this date will be returned. 
     * @param periodEnd Optional. Only periods starting before this date will be returns 
     * If periodStart is not given, all available periods starting before this date will be returned.
     */
    void getInUseLimitForecasts(
    		ForecastSnapshotReceiver receiver,
    		Instant offsetPeriodStart,
    		Instant periodEnd);
    
    /**
     * Execute a request for the current forecast limits with a streaming response handler
     * 
     * @param receiver Streaming data receiver for snapshot data
     * @param monitoringSet filter for monitoring set name
     * @param offsetPeriodStart Optional. Only periods starting at or after this date will be returned. 
     * @param periodEnd Optional. Only periods starting before this date will be returns 
     * If periodStart is not given, all available periods starting before this date will be returned. 
     */
    void getInUseLimitForecasts(
    		ForecastSnapshotReceiver receiver,
    		String monitoringSet,
    		Instant offsetPeriodStart,
    		Instant periodEnd);
    
    /**
     * Create a polling subscription for forecast snapshot data updates
     * 
     * @param receiver Streaming data receiver for snapshot data
     * @param monitoringSet filter for monitoring set name 
     * @param pollingRateMillis Interval in millis between polling loops
     * @return
     */
    ForecastSnapshotSubscribedRequest subscribeToInUseLimitForecastUpdates(
    		ForecastSnapshotSubscribedReceiver receiver,
    		String monitoringSet,
    		int pollingRateMillis);

    
    /**
     * Create a polling subscription for forecast snapshot data updates
     * 
     * @param receiver Streaming data receiver for snapshot data
     * @param pollingRateMillis Interval in millis between polling loops
     * @return
     */
    ForecastSnapshotSubscribedRequest subscribeToInUseLimitForecastUpdates(
    		ForecastSnapshotSubscribedReceiver receiver,
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
    void getInUseLimits(
            RealTimeSnapshotReceiver receiver,
            String monitoringSet,
            String transmissionFacility);

    /**
     * Execute a request for the current real-time limits with a streaming response handler
     * 
     * @param receiver
     * @param monitoringSet
     */
    void getInUseLimits(
            RealTimeSnapshotReceiver receiver,
            String monitoringSet);

    /**
     * Execute a request for the current real-time limits with a streaming response handler
     * 
     * @param receiver
     */
    void getInUseLimits(
            RealTimeSnapshotReceiver receiver);
    
    /**
     * Create a polling subscription for real-time snapshot data updates
     * 
     * @param receiver Streaming data receiver for snapshot data
     * @param pollingRateMillis Interval in millis between polling loops
     * @return
     */
    RealTimeSnapshotSubscribedRequest subscribeToInUseLimits(
    		RealTimeSnapshotSubscribedReceiver receiver,
    		int pollingRateMillis);
    
    /**
     * Create a polling subscription for real-time snapshot data updates
     * 
     * @param receiver Streaming data receiver for snapshot data
     * @param monitoringSet optional filter for monitoring set
     * @param pollingRateMillis Interval in millis between polling loops
     * @return
     */
    RealTimeSnapshotSubscribedRequest subscribeToInUseLimits(
    		RealTimeSnapshotSubscribedReceiver receiver,
    		String monitoringSet,
    		int pollingRateMillis);
    
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

    /**
     * Un-subscribe an active polling request
     *
     * @param subscription
     */
    void unsubscribe(RequestSubscription subscription);

    /**
     * Un-subscribe all active polling requests
     */
    void unsubscribeAll();

    static TrolieClientBuilder builder(String baseUrl, HttpClient httpClient) {
        return new TrolieClientBuilder(baseUrl, httpClient);
    }
}
