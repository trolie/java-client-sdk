package org.trolie.client.examples;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trolie.client.RequestSubscription;
import org.trolie.client.TrolieClient;
import org.trolie.client.TrolieClientBuilder;
import org.trolie.client.exception.StreamingGetException;
import org.trolie.client.model.operatingsnapshots.ForecastPeriodSnapshot;
import org.trolie.client.model.operatingsnapshots.ForecastSnapshotHeader;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscribedReceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This example uses the TROLIE client to create an event-based subscription to
 * forecast snapshots.
 */
public class SubscribeToForecastSnapshots {

    private static final Logger logger = LoggerFactory.getLogger(SubscribeToForecastSnapshots.class);

    private static TrolieClient initializeClient() {
        return new TrolieClientBuilder("http://localhost:8080",
                HttpClients.createDefault())
                // By default, the client will store the "ETag", representing the current
                // version of the ratings that this application has, in memory.
                // For many applications, this is appropriate.  However, you may
                // wish to replace it with your own more permanent store.
                // .etagStore(new MemoryETagStore())
                // The client polls for new rating data using the
                // pattern described in
                // https://trolie.energy/articles/conditional-GET.html
                // The rate at which the TROLIE server is polled for forecast ratings
                // may be controlled.
                // .forecastRatingsPollMs(10000)
                .build();
    }

    public static void main(String[] args) throws IOException {

        logger.info("Set up subscription to forecast snapshots");
        try (TrolieClient client = initializeClient();
             var reader = new BufferedReader(
                     new InputStreamReader(System.in))) {

            // The in-use rating forecast may also be fetched directly.  However,
            // most applications will need to regularly update their ratings based on the ratings
            // determined by the clearinghouse.  By implementing a subscriber, we are notified
            // and can handle new rating snapshots as we discover them.
            //
            // Technically, this subscriber gets invoked on a separate thread pool when new ratings
            // are available.
            var subscriber = new LimitSubscriber();
            client.subscribeToInUseLimitForecastUpdates(subscriber);

            // The subscription process runs in the background within the program.
            System.err.println("Hit enter to terminate program");
            reader.readLine();

            // The subscriptions will also get cleaned up automatically when the client
            // itself is closed.  However, they may also be explicitly unsubscribed as shown here.
            client.unsubscribe(subscriber.subscription);

        }


    }

    private static class LimitSubscriber implements ForecastSnapshotSubscribedReceiver {

        private RequestSubscription subscription;

        // Snapshots are consumed in steps.
        // Implementations of these receiver interfaces handle each piece
        // of the snapshot received individually.  This is intended to mirror the
        // pattern used by event-based JSON and XML parsers, where we avoid forcing
        // the need to have
        // the entire snapshot in memory at once due to its potential size.
        //
        // The methods below are laid out in the order they will be invoked when a
        // new snapshot is received.

        @Override
        public void beginSnapshot() {
            // This first method allows the application to prime any resources necessary
            // to handle a new snapshot.  Reset buffers, start database transactions, etc.
            logger.info("New forecast snapshot received");
        }

        @Override
        public void header(ForecastSnapshotHeader header) {
            // The header tells the client application about the snapshot.
            // The most relevant information is always the target begin time for the forecast
            // window.
            var windowBegin = header.getBegins();
            logger.info("Got snapshot for window {}", windowBegin);

            // The source section describes the provenance of the snapshot.
            // It may or may not be needed by the client application, but is useful
            // in interop and traceability.
            var provenance = header.getSource();
            logger.info("Snapshot Id {} generated by {} at {}",
                    provenance.getOriginId(),
                    provenance.getProvider(),
                    provenance.getLastUpdated());

            // The clearinghouse must explicitly define the list of default emergency durations included
            // in the snapshot.  For most clearinghouse providers, this list will be relatively static and
            // pre-coordinated. However, it is included in the snapshot as a clue to interop, and
            // intelligent applications may reference it dynamically if it makes sense to do so.
            var emergencyRatings = header.getDefaultEmergencyRatingDurations();
            for(var emergencyRating : emergencyRatings) {
                logger.info("Snapshot includes emergency rating {} with duration {} minutes",
                        emergencyRating.getName(), emergencyRating.getDurationMinutes());
            }

            // Optionally, the TROLIE service provider may also provide the list of power system
            // resources included in the snapshot, with their known aliases.  This
            // may be useful in debugging model mismatch issues.
            for(var rsrc : header.getPowerSystemResources()) {
                logger.info("Snapshot includes resource {} with aliases {}", rsrc.getResourceId(),
                        rsrc.getAlternateIdentifiers());
            }

        }

        private String currentResourceId;

        @Override
        public void beginResource(String resourceId) {
            // After handling the header, the ratings themselves will be processed.
            // Each rating schedule is broken up by resource.  This method gets invoked when a new resource is
            // encountered.
            logger.info("Accepting ratings for resource {}", resourceId);
            this.currentResourceId = resourceId;
        }

        @Override
        public void period(ForecastPeriodSnapshot period) {
            // Invoked when an individual rating / period combination is processed.
            logger.info("""
                    Ratings for period starting {}:
                    continuous MVA: {}
                    emergency ratings: {}""",
                    period.getPeriodStart(),
                    period.getContinuousOperatingLimit().getMVA(),
                    period.getEmergencyOperatingLimits());
        }

        @Override
        public void endResource() {
            // Gets invoked once all rating periods are handled for this resource.
            logger.info("Finished accepting ratings for resource {}", currentResourceId);
        }

        @Override
        public void endSnapshot() {
            // Calls when snapshot processing has completed successfully.
            // Useful to flush buffers, commit database transactions, etc.
            logger.info("Finished processing snapshot");
        }

        // CLERICAL METHODS: These next two methods
        // sit outside the handling of a successfully downloaded snapshot

        @Override
        public void error(StreamingGetException t) {
            // Invoked to add any custom processing on errors received while Invoking the TROLIE server.
            logger.error("Unexpected exception fetching snapshots: {}", t.getMessage(), t);
        }

        @Override
        public void setSubscription(RequestSubscription subscription) {
            this.subscription = subscription;
        }
    }




}
