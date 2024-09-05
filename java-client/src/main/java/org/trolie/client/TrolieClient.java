package org.trolie.client;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
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

    void subscribeToInUseLimitForecastUpdates();

    ForecastRatingProposalStreamingUpdate createForecastRatingProposalStreamingUpdate();

    void getInUseLimits();

    void subscribeToInUseLimits();

    void updateRealTimeProposal();

    static TrolieClientBuilder builder(String baseUrl, HttpClientBuilder clientBuilder) {
        return new TrolieClientBuilder(baseUrl, clientBuilder);
    }
}
