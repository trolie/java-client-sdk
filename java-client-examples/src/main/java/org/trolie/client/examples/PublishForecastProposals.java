package org.trolie.client.examples;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trolie.client.TrolieClient;
import org.trolie.client.TrolieClientBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * This example uses the TROLIE client to publish some 240-hour ahead
 * forecast ratings.
 */
public class PublishForecastProposals {

    private static final Logger logger = LoggerFactory.getLogger(PublishForecastProposals.class);

    private static TrolieClient initializeClient() {
        return new TrolieClientBuilder("http://localhost:8080",
                HttpClients.createDefault()).build();
    }

    private static final String RATINGS_FILE = "/forecast-ratings-out.csv";


    public static void main(String[] args) throws URISyntaxException, IOException {

        logger.info("Instantiating client");
        try (var client = initializeClient()) {

            // Absolute file URI.  Normally one could just pull from a direct file.
            var resource = PublishForecastProposals.class.
                    getResource(RATINGS_FILE);
            if(resource == null) {
                throw new IllegalArgumentException("Cannot find classpath resource " + RATINGS_FILE);
            }
            File dataFile = new File(resource.toURI());
            logger.info("Streaming proposal from CSV data in file {}", dataFile.getAbsolutePath());



        }


    }
}
