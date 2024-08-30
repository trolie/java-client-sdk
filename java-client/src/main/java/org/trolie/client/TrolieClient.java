package org.trolie.client;

import java.util.Iterator;

import org.trolie.client.model.ratingproposals.ForecastProposalHeader;
import org.trolie.client.model.ratingproposals.ForecastRating;
import org.trolie.client.model.ratingproposals.ForecastRatingProposalStatus;
import org.trolie.client.ratingproposals.ForcastRatingProposalUpdate;

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

    ForcastRatingProposalUpdate createForecastProposalUpdate();

    void getInUseLimits();

    void subscribeToInUseLimits();

    void updateRealTimeProposal();


}
