package org.trolie.client;

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

    void subscribeToInUseLimitForecastUpdates();

    void updateForecastProposal();

    void getInUseLimits();

    void subscribeToInUseLimits();

    void updateRealTimeProposal();


}
