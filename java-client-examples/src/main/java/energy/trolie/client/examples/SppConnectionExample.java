package energy.trolie.client.examples;

import energy.trolie.client.TrolieClient;
import energy.trolie.client.TrolieClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SppConnectionExample {

    public static void main(String[] args) throws IOException {
        try (TrolieClient client = new TrolieClientBuilder(
                "http://localhost:8080",
                HttpClients.createDefault())
                .withSppAuthentication(
                        "TestScreenName",
                        Base64.getEncoder().encodeToString("secretkey".getBytes(StandardCharsets.UTF_8)))
                .build()) {

            // Client is now ready to interact with SPP system
            }
    }
}