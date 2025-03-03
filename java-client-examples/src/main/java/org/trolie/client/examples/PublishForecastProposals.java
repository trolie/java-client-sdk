package org.trolie.client.examples;

import org.apache.commons.csv.CSVFormat;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trolie.client.TrolieClient;
import org.trolie.client.TrolieClientBuilder;
import org.trolie.client.model.common.AlternateIdentifier;
import org.trolie.client.model.common.DataProvenance;
import org.trolie.client.model.common.EmergencyRatingDuration;
import org.trolie.client.model.common.PowerSystemResource;
import org.trolie.client.model.ratingproposals.ForecastProposalHeader;
import org.trolie.client.request.ratingproposals.ForecastRatingProposalUpdate;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This example uses the TROLIE client to publish some 240-hour ahead
 * forecast ratings.
 */
public class PublishForecastProposals {

    private static final Logger logger = LoggerFactory.getLogger(PublishForecastProposals.class);

    private static TrolieClient initializeClient() {
        return new TrolieClientBuilder("http://localhost:8080",
                HttpClients.createDefault())
                // If the TROLIE server supports test mode features, then
                // this may be used to simulate a particular identity in
                // non-prod environments where test mode is enabled.
                // The "TO1" user is the rating provider for all
                // the test data provided in this example.
                .httpHeaders(Map.of(
                        "X-TROLIE-Testing-Identity", "TO1"))
                .build();
    }

    private static final String RATINGS_FILE = "/forecast-ratings-out.csv";
    private static final String RESOURCES_FILE = "/power-system-resources.csv";


    // Up front, we define the types of emergency rating durations we intend to send.
    // This must be sent explicitly in the TROLIE.
    // Check the TROLIE implementer for what they will accept.
    private static final EmergencyRatingDuration EMERGENCY =
            EmergencyRatingDuration.of("EMERG", 120);
    private static final EmergencyRatingDuration LOADSHED =
            EmergencyRatingDuration.of("LDSHD", 15);
    private static final EmergencyRatingDuration DAL =
            EmergencyRatingDuration.of("DAL", 5);

    private static final List<EmergencyRatingDuration> EMERGENCY_RATING_DURATIONS
            = Arrays.asList(EMERGENCY, LOADSHED, DAL);



    public static void main(String[] args) throws URISyntaxException, IOException {

        // When sending forecasts, it is critically important that we tell the TROLIE server the explicit
        // start time of the forecast window.  The rules for the cut-off time will differ by TROLIE
        // implementer, so be sure to check.
        // For the purpose of this example, we will submit to the next hour that is at least 30 minutes
        // from now.
        var forecastWindowBegin =
                LocalDateTime.now()
                        .atZone(ZoneId.systemDefault())
                        .plus(Duration.ofMinutes(90))
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0).toInstant();

        logger.info("Begin writing forecast proposal for forecast window {}", forecastWindowBegin);
        try (var client = initializeClient();
             var proposalSubmission = client.createForecastRatingProposalStreamingUpdate()) {

            // Set header data.
            sendHeader(proposalSubmission, forecastWindowBegin);

            // Set ratings.
            sendRatings(proposalSubmission);

            logger.info("Finalizing proposal submission");

            // This call will finalize the proposal submission, and wait for a response from
            // the server.  On successful communication to the TROLIE server, this status
            // object will be returned.
            //
            // TROLIE forecast submissions are designed to accept partial results.  Therefore,
            // return of a status object does not guarantee that all ratings were submitted successfully,
            // or that the ratings were complete.  Callers should check the details of the status message
            // to monitor whether the proposal was successful.
            var status = proposalSubmission.complete();
            logger.info("Successfully submitted proposal, with {} submission " +
                    "errors and {} incomplete obligations remaining.",
                    status.getInvalidProposalCount(), status.getIncompleteObligationCount());

            // The errors represent something we did wrong with the submission for each individual
            // resource.  This could indicate that the resource Id was wrong, that the submitted ratings
            // violated validation thresholds, or that the emergency ratings hierarchy was incorrect, for
            // example.
            for(var err : status.getProposalValidationErrors()) {
                logger.error("Validation Error on {}: {}", err.getResourceId(), err.getMessage());
            }

            // The incomplete obligations state resources that the TROLIE server was expecting for this forecast
            // window that we haven't successfully provided yet.  This could be because the proposal we sent
            // was invalid, in which case it would be included in the validation errors above.
            // However, it may also be due to resources that the TROLIE server is expecting from us that we missed.
            //
            // For a given forecast window, clients are allowed to break their proposal into multiple batches
            // as needed across the number of resources they have.  This may be good practice performance-wise for
            // rating providers with large numbers of resources (typically > 500).
            // However, ideally, at the end of submitting all the batches, the incomplete obligation count
            // should go to 0.  If not, this indicates a mismatch of expectations between us and the TROLIE
            // server owner as to which resources we should be providing ratings for.
            for(var ob : status.getIncompleteObligations()) {
                logger.warn("Outstanding incomplete obligation for {}", ob.getResourceId());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            System.exit(-1);
        }

        System.exit(0);


    }

    private static void sendHeader(ForecastRatingProposalUpdate proposalSubmission,
                                   Instant forecastWindowBegin) throws IOException, URISyntaxException {

        // The forecast proposal begins with a header.  This header
        // is important for establishing intent of the submission, tracking information,
        // and interoperability hints to the server
        var header = ForecastProposalHeader.builder()

                // We always need to explicitly define the intended begin time of the forecast
                // window.
                .begins(forecastWindowBegin)

                // The data provenance section helps the TROLIE server explicitly
                // capture where this data came from, and when it was originally generated.
                // The builder class used below defaults the update time to the current time.
                .source(DataProvenance.builder()
                        // This should be the identifier for your system.
                        // It could be your company's NERC ID, although
                        // it may also make sense for this to be the name of a particular
                        // system.
                        .provider("YOURCOMPANYNAMEHERE")
                        // This uniquely identifies the submission, which can be useful
                        // for correlating log messages.
                        // The company prefix below is optional.
                        // UUIDs are a good way to ensure the submission is unique.
                        .originId("yourcompanysystem:" + UUID.randomUUID())
                        .build())

                // We declare our emergency ratings durations used up front.
                .defaultEmergencyRatingDurations(EMERGENCY_RATING_DURATIONS)

                // Finally, we must declare the power system resources
                // we will include in this proposal, and any aliases we are aware of.
                .powerSystemResources(getPowerSystemResources())
                .build();

        proposalSubmission.begin(header);

    }

    private static List<PowerSystemResource> getPowerSystemResources() throws IOException, URISyntaxException {
        var psrList = new ArrayList<PowerSystemResource>();

        var uri = PublishForecastProposals.class.
                getResource(RESOURCES_FILE);
        if(uri == null) {
            throw new IllegalArgumentException("Cannot find classpath resource " + RATINGS_FILE);
        }
        var file = new File(uri.toURI());


        // Here, we build up a list of power system resources and their aliases from a file.
        // The source data for this may change depending on the system.  This often comes
        // from network modeling environments for larger utilities, or it could just be a
        // simple set of "standing" data for smaller systems.
        //
        // The aliases are of critical importance for debugging, as the TROLIE server implementer
        // will almost always use different names for the resources than you do.
        // Note that the primary "resourceId" must be the specific name for the resource
        // that the TROLIE server implementer expects.
        logger.info("Begin reading power system resource list from {}", file);
        try(var fReader = new FileReader(file);
            var csvParser = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(fReader)) {

            String currentResource = null;
            ArrayList<AlternateIdentifier> currentAliases = null;
            for(var line : csvParser) {

                String resourceId = line.get("resource");
                if(!resourceId.equals(currentResource)) {
                    currentResource = resourceId;
                    currentAliases = new ArrayList<>();
                    var psr = PowerSystemResource.builder()
                            .resourceId(resourceId)
                            .alternateIdentifiers(currentAliases)
                            .build();
                    psrList.add(psr);

                }

                // There are a number of ways to define aliases.
                // This sample data set just includes a name, and name type,
                // analogous to the NameType reference in IEC CIM.  This may often be used
                // to indicate different types of systems.  In addition,
                // users may pass an alternate "authority", which should represent the
                // company responsible for coming up with the name, such as "MISO", "ISONE",
                // or "SPP".
                //
                // An alternative MRID associated with that alias may also be provided
                currentAliases.add(
                   AlternateIdentifier.builder()
                        .name(line.get("alias"))
                        .type(line.get("alias_name_type"))
                        //.authority(line.get("authority"))
                        //.mrid(line.get("mrid"))
                        .build());
            }
        }

        return psrList;
    }

    private static void sendRatings(ForecastRatingProposalUpdate proposalSubmission)
            throws IOException, URISyntaxException {
        // Absolute file URI.  Normally one could just pull from a direct file.
        var resource = PublishForecastProposals.class.
                getResource(RATINGS_FILE);
        if(resource == null) {
            throw new IllegalArgumentException("Cannot find classpath resource " + RATINGS_FILE);
        }

        File dataFile = new File(resource.toURI());
        logger.info("Begin sending ratings data from {}", dataFile);
        try (var fileReader = new FileReader(dataFile);
             var csvParser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .build()
                     .parse(fileReader)) {

            // The CSV format is used here because it is familiar, and
            // an easy translation from most OT systems.  This
            // file uses a format common to Microsoft Excel, with simple
            // column headers.
            //
            // The TROLIE client should receive ratings from your ratings
            // forecast system in some way.  This example program uses
            // a static CSV file in hopes it is easy to follow and adapt.

            // Unlike the header data, which is loaded into memory all at once, this
            // data is written in chunks.  This enables us not to have the entire
            // forecast in memory at once, but rather stream it from one source to another.
            String currentResource = null;

            for(var line : csvParser) {
                String resourceId = line.get("name");

                // Each forecast is given one resource at a time, so we must first send events
                // to note where the data for a particular resource starts and ends.
                if(!resourceId.equals(currentResource)) {
                    if(currentResource != null) {
                        // End the prior resource.
                        proposalSubmission.endResource();
                    }
                    currentResource = resourceId;
                    proposalSubmission.beginResource(currentResource);
                }

                // Now, we send a DTO with the forecast for this period,
                // for this resource.
                var pb = proposalSubmission.periodBuilder();

                // The period API is 0-based
                int hour = Integer.parseInt(line.get("hour")) - 1;
                pb.setPeriod(hour);

                pb.setContinuousMVA(Float.parseFloat(line.get("continuous")));
                for(var band : EMERGENCY_RATING_DURATIONS) {
                    pb.setEmergencyMVA(band.getName(),
                            Float.parseFloat(line.get(band.getName())));
                }
                pb.complete();
            }

            // End the last resource.
            proposalSubmission.endResource();


        }
    }

}
