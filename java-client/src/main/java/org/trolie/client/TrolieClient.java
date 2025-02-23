package org.trolie.client;

import lombok.NonNull;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.trolie.client.request.monitoringsets.MonitoringSetsReceiver;
import org.trolie.client.request.monitoringsets.MonitoringSetsSubscribedReceiver;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotReceiver;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscribedReceiver;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotReceiver;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotSubscribedReceiver;
import org.trolie.client.request.ratingproposals.ForecastRatingProposalUpdate;
import org.trolie.client.request.ratingproposals.RealTimeRatingProposalUpdate;

import java.io.Closeable;
import java.time.Instant;


/**
 * <p>Primary TROLIE client access interface.  Represents configured access to a single
 * TROLIE server.  Should be used as a singleton within applications, at least for each
 * TROLIE endpoint used by a given application.</p>
 * <p>Users should note that the TrolieClient is optimized for performance, particularly
 * for users with large data sets.  Therefore, most operations use a streaming approach
 * for receipt of data components, rather than force applications to keep entire structures in
 * memory at once.  </p>
 * <h2>Using TrolieClient Methods</h2>
 * <p>For consistency, the TrolieClient API uses the same patterns regardless of
 * the size of the data users may be using.  These patterns always use reactive-style callbacks,
 * and typically involve streaming mechanisms for breaking each payload into logical slices.
 * This may be more difficult to program for users, but helps ensure that users of the SDK are
 * more likely to have successful implementations, even as data sizes increase.</p>
 * <p>The methods below make use of the following common constructs:</p>
 * <ul>
 *     <li>{@link StreamingResponseReceiver}s are callbacks to get data from the TROLIE server.
 *     Instead of returning the entire payload of response in memory, the receivers will have various methods
 *     that are invoked as parsed data is returned.  API users must implement these interfaces to receive data.
 *     This follows a similar pattern to event-based parsing systems,
 *     popularized most in Java by the SAX XML parser.</li>
 *     <li>{@link RequestSubscription}s are returned from methods that set up a polling loop within the client.
 *     The TROLIE client implements the mechanics under the hood to poll for TROLIE servers using
 *     <a href="https://trolie.energy/articles/conditional-GET.html">Conditional GETs</a>, and will then
 *     notify associated {@link StreamingResponseReceiver}s as new data is returned.  These subscriptions may
 *     be managed using the {@link #unsubscribe(RequestSubscription)} and {@link #unsubscribeAll()}
 *     methods.</li>
 *     <li>{@link StreamingUpdate}s are the inverse of the receivers.  Rather than send entire payloads at once
 *     in a giant in-memory structure, these classes allow the API user to send the data in small chunks and stream
 *     it out.  Each StreamingUpdate assumes that its methods are invoked in a particular order matching the structure
 *     of the TROLIE operation being invoked.  </li>
 * </ul>
 * @see TrolieClientBuilder
 */
public interface TrolieClient extends Closeable {

    /**
     * Synchronously fetch the definition of a monitoring set.
     *
     * @param receiver receiver for monitoring set contents.
     * @param monitoringSet monitoring set identifier.
     */
    void getMonitoringSet(
    		@NonNull MonitoringSetsReceiver receiver,
            @NonNull String monitoringSet);

    /**
     * Create a polling subscription for updates to the given monitoring set.
     *
     * @param receiver receives updates to the monitoring set as they occur.
     *                 Note that these invocations will be on the polling thread,
     *                 so long-running operations will block additional polls for
     *                 data update.
     * @param monitoringSet the monitoring set identifier to poll for
     * @return handle to the request.
     */
    RequestSubscription subscribeToMonitoringSetUpdates(
    		MonitoringSetsSubscribedReceiver receiver,
            String monitoringSet);

    /**
     * Identical to {@link #getMonitoringSet(MonitoringSetsReceiver, String)},
     * but simply fetches this grid operator's default monitoring set rather than one specified
     * with a particular name.
     *
     * @param receiver receiver for the monitoring set
     */
    void getDefaultMonitoringSet(
    		MonitoringSetsReceiver receiver);

    /**
     * Identical to
     * {@link #subscribeToMonitoringSetUpdates(MonitoringSetsSubscribedReceiver, String, int)},
     * but creates a polling subscription for this grid operator's default monitoring
     * set rather than one specified
     * with a particular name.
     *
     * @param receiver receives updates to the monitoring set as they occur.
     *                 Note that these invocations will be on the polling thread,
     *                 so long-running operations will block additional polls for
     *                 data update.
     * @return handle to the request.
     */
    RequestSubscription subscribeToDefaultMonitoringSetUpdates(
    		MonitoringSetsSubscribedReceiver receiver);

    /**
     * Execute a synchronous
     * request for the current forecast limits with a streaming response handler,
     * assuming this user's default monitoring set.
     * 
     * @param receiver Streaming data receiver for snapshot data
     */
    void getInUseLimitForecasts(
    		ForecastSnapshotReceiver receiver);
    
    /**
     * Execute a request for the current forecast limits with a streaming response handler
     * with the given monitoring set.
     * 
     * @param receiver Streaming data receiver for snapshot data
     * @param monitoringSet filter for monitoring set name
     */
    void getInUseLimitForecasts(
    		ForecastSnapshotReceiver receiver,
    		String monitoringSet);

    /**
     * Execute a request for the current forecast limits with a streaming response handler,
     * given this user's default monitoring set.
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
     * @param resourceId Only return limits for this power system resource
     * @param offsetPeriodStart Optional. Only periods starting at or after this date will be returned. 
     * @param periodEnd Optional. Only periods starting before this date will be returns 
     * If periodStart is not given, all available periods starting before this date will be returned. 
     */
    void getInUseLimitForecasts(
    		ForecastSnapshotReceiver receiver,
    		String monitoringSet,
            String resourceId,
    		Instant offsetPeriodStart,
    		Instant periodEnd);
    
    /**
     * Create a polling subscription for forecast snapshot data updates
     * 
     * @param receiver Streaming data receiver for snapshot data
     * @param monitoringSet filter for monitoring set name
     * @return request handle
     */
    RequestSubscription subscribeToInUseLimitForecastUpdates(
    		ForecastSnapshotSubscribedReceiver receiver,
    		String monitoringSet);

    
    /**
     * Create a polling subscription for forecast snapshot data updates
     * 
     * @param receiver Streaming data receiver for snapshot data
     * @return request handle
     */
    RequestSubscription subscribeToInUseLimitForecastUpdates(
    		ForecastSnapshotSubscribedReceiver receiver);

    /**
     * Execute a request for the current forecast limits with a streaming response handler
     *
     * @param receiver Streaming data receiver for snapshot data
     */
    void getRegionalLimitsForecast(
            ForecastSnapshotReceiver receiver);

    /**
     * Execute a request for the regionally limiting forecast limits with a streaming response handler
     *
     * @param receiver Streaming data receiver for snapshot data
     * @param monitoringSet filter for monitoring set name
     */
    void getRegionalLimitsForecast(
            ForecastSnapshotReceiver receiver,
            String monitoringSet);

    /**
     * Execute a request for the regionally limiting forecast limits with a streaming response handler
     *
     * @param receiver Streaming data receiver for snapshot data
     * @param offsetPeriodStart Optional. Only periods starting at or after this date will be returned.
     * @param periodEnd Optional. Only periods starting before this date will be returns
     * If periodStart is not given, all available periods starting before this date will be returned.
     */
    void getRegionalLimitsForecast(
            ForecastSnapshotReceiver receiver,
            Instant offsetPeriodStart,
            Instant periodEnd);

    /**
     * Execute a request for the regionally limiting forecast limits with a streaming response handler
     *
     * @param receiver Streaming data receiver for snapshot data
     * @param monitoringSet filter for monitoring set name
     * @param resourceId Only return limits for this power system resource
     * @param offsetPeriodStart Optional. Only periods starting at or after this date will be returned.
     * @param periodEnd Optional. Only periods starting before this date will be returns
     * If periodStart is not given, all available periods starting before this date will be returned.
     */
    void getRegionalLimitsForecast(
            ForecastSnapshotReceiver receiver,
            String monitoringSet,
            String resourceId,
            Instant offsetPeriodStart,
            Instant periodEnd);

    /**
     * Create a polling subscription for regionally limiting forecast snapshot data updates
     *
     * @param receiver Streaming data receiver for snapshot data
     * @return request handle
     */
    RequestSubscription subscribeToRegionalLimitsForecast(
            ForecastSnapshotSubscribedReceiver receiver);

    /**
     * Create a polling subscription for regionally limiting forecast snapshot data updates
     *
     * @param receiver Streaming data receiver for snapshot data
     * @param monitoringSet filter for monitoring set name
     * @return request result handle
     */
    RequestSubscription subscribeToRegionalLimitsForecast(
            ForecastSnapshotSubscribedReceiver receiver,
            String monitoringSet);
    
    /**
     * Create a forecast proposal update that can stream the update submission to the server
     * 
     * @return update handle
     */
    ForecastRatingProposalUpdate createForecastRatingProposalStreamingUpdate();



    /**
     * Execute a request for the current real-time limits with a streaming response handler
     * 
     * @param receiver streaming receiver for real-time limits
     * @param monitoringSet monitoring set name.  Either monitoring set or
     *                      resourceId may be provided, but not both.
     *                      If neither are provided, the user's default
     *                      monitoring set is assumed.
     * @param resourceId a resource to filter by.  Either resource ID or
     *                   monitoring set name may be provided, but not both.
     *                   If neither are provided, then the user's default
     *                   monitoring set will be assumed.
     */
    void getInUseLimits(
            RealTimeSnapshotReceiver receiver,
            String monitoringSet,
            String resourceId);

    /**
     * Execute a request for the current real-time limits
     * with a streaming response handler
     * 
     * @param receiver streaming receiver for real-time limits
     * @param monitoringSet monitoring set name.  If not
     *                      provided, the user's default
     *                      monitoring set is assumed.
     */
    void getInUseLimits(
            RealTimeSnapshotReceiver receiver,
            String monitoringSet);

    /**
     * Execute a request for the current real-time limits
     * with a streaming response handler
     * 
     * @param receiver stream handler
     */
    void getInUseLimits(
            RealTimeSnapshotReceiver receiver);
    
    /**
     * Create a polling subscription for real-time snapshot data updates
     * 
     * @param receiver Streaming data receiver for snapshot data
     * @return subscription result
     */
    RequestSubscription subscribeToInUseLimits(
    		RealTimeSnapshotSubscribedReceiver receiver);
    
    /**
     * Create a polling subscription for real-time snapshot data updates
     * 
     * @param receiver Streaming data receiver for snapshot data
     * @param monitoringSet optional filter for monitoring set
     * @return subscription handle
     */
    RequestSubscription subscribeToInUseLimits(
    		RealTimeSnapshotSubscribedReceiver receiver,
    		String monitoringSet);
    
    /**
     * Create a polling subscription for real-time snapshot data updates
     * 
     * @param receiver Streaming data receiver for snapshot data
     * @param monitoringSet optional filter for monitoring set
     * @param resourceId optional filter for transmission facility
     * @return subscription handle
     */
    RequestSubscription subscribeToInUseLimits(
    		RealTimeSnapshotSubscribedReceiver receiver,
    		String monitoringSet,
    		String resourceId);

    /**
     * Execute a request for the regional real-time limits
     * with a streaming response handler.
     * Returns limits assuming the default monitoring set in the
     * remote TROLIE.
     *
     * @param receiver callback for new limit snapshots
     */
    void getRegionalRealTimeLimits(
            RealTimeSnapshotReceiver receiver);

    /**
     * Execute a request for the regional real-time limits with a streaming response handler
     *
     * @param receiver callback for new limit snapshots
     * @param monitoringSet monitoring set to filter by.
     */
    void getRegionalRealTimeLimits(
            RealTimeSnapshotReceiver receiver,
            String monitoringSet);

    /**
     * Execute a request for the regional real-time limits with
     * a streaming response handler
     *
     * @param receiver callback for new limit snapshots
     * @param monitoringSet monitoring set to filter by.
     * @param resourceId resource ID filter
     */
    void getRegionalRealTimeLimits(
            RealTimeSnapshotReceiver receiver,
            String monitoringSet,
            String resourceId);

    /**
     * Create a polling subscription regional real-time snapshot data updates
     *
     * @param receiver Streaming data receiver for regional snapshot data
     * @return subscription handle.
     */
    RequestSubscription subscribeToRegionalRealTimeLimits(
            RealTimeSnapshotSubscribedReceiver receiver);

    /**
     * Create a polling subscription for regional real-time snapshot data updates
     *
     * @param receiver Streaming data receiver for regional snapshot data
     * @param monitoringSet optional filter for monitoring set
     * @return subscription handle
     */
    RequestSubscription subscribeToRegionalRealTimeLimits(
            RealTimeSnapshotSubscribedReceiver receiver,
            String monitoringSet);

    /**
     * Create a real-time proposal update that can stream the update submission to the server
     * 
     * @return stateful request.
     */
    RealTimeRatingProposalUpdate createRealTimeRatingProposalStreamingUpdate();

    /**
     * Un-subscribe an active polling request
     *
     * @param subscription subscription to cancel.
     */
    void unsubscribe(RequestSubscription subscription);

    /**
     * Un-subscribe all active polling requests
     */
    void unsubscribeAll();

    /**
     * Initializes a new builder.
     * @param baseUrl URL to the TROLIE service, such as https:&#47;&#47;trolie.example.com.
     * @param httpClient a pre-configured Apache HTTP client.
     *                   The TROLIE client is built on top of the Apache HTTP client, and the full suite
     *                   of configuration options for it are available.  See examples for more detail.
     */
    static TrolieClientBuilder builder(String baseUrl, CloseableHttpClient httpClient) {
        return new TrolieClientBuilder(baseUrl, httpClient);
    }

}
