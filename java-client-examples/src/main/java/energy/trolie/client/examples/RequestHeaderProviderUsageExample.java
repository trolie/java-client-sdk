package energy.trolie.client.examples;

import energy.trolie.client.RequestHeaderProvider;
import energy.trolie.client.TrolieClient;
import energy.trolie.client.TrolieClientBuilder;
import energy.trolie.client.TrolieRequestContext;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * This example demonstrates how to implement and register a custom {@link RequestHeaderProvider}
 * with the {@link TrolieClientBuilder}.
 * <p>
 * A {@link RequestHeaderProvider} allows the client to supply dynamic, request-time headers
 * (like computed security signatures, authentication tokens, nonces, or timestamps) that may
 * vary based on the request URI, method, or content type.
 */
public class RequestHeaderProviderUsageExample {

    private static final Logger logger = LoggerFactory.getLogger(RequestHeaderProviderUsageExample.class);

    private static TrolieClient initializeClient() {
        // 1. Instantiate the custom header provider
        RequestHeaderProvider dynamicHeaderProvider = new CustomTokenHeaderProvider("my-api-key-secret", "user-screen-name");

        // 2. Build the TrolieClient with the provider registered
        return new TrolieClientBuilder("http://localhost:8080", HttpClients.createDefault())
                // Register our custom dynamic header provider.
                // It will be executed at request-time for every HTTP request the client performs,
                // receiving the final request method, URI, and content type.
                .addRequestHeaderProvider(dynamicHeaderProvider)
                .build();
    }


    public static void main(String[] args) {
        logger.info("Initializing TrolieClient with dynamic request header provider...");

        try (TrolieClient client = initializeClient()) {
            logger.info("TrolieClient built successfully!");
            logger.info("The custom RequestHeaderProvider is now registered on the client.");
            logger.info("Every outgoing request (GET, POST, PATCH, etc.) will dynamically trigger " +
                        "the RequestHeaderProvider, allowing it to inject fresh dynamic headers.");
        } catch (Exception e) {
            logger.error("Error creating or closing client", e);
        }
    }

    /**
         * A sample implementation of {@link RequestHeaderProvider} that generates dynamic,
         * request-specific authentication/authorization headers.
         * <p>
         * For instance, if a server requires a dynamic HMAC signature over the request path
         * and a unique nonce (such as a multifactor authentication token), this provider
         * can compute it on-the-fly when the request is executed.
         */
        private record CustomTokenHeaderProvider(String secretKey, String screenName) implements RequestHeaderProvider {

        @Override
            public Map<String, String> headersFor(TrolieRequestContext requestContext) {
                // This is called by the SDK at request execution time.
                // We have access to the final request method, URI, and content-type.
                String method = requestContext.method();
                URI requestUri = requestContext.uri();
                String path = requestUri.getPath().toLowerCase();

                // Generate a unique nonce
                String nonce = UUID.randomUUID().toString();

                // Capture the current timestamp
                String timestamp = Instant.now().toString();

                // Compute a digital signature over the request-specific details
                String signature = generateSignature(nonce, timestamp, method, path, secretKey);

                logger.info("Computing headers for {} request to {}", method, path);

                // Return the key-value pairs representing the custom headers to add/override.
                return Map.of(
                        "X-Request-Nonce", nonce,
                        "X-Request-Timestamp", timestamp,
                        "X-Request-Signature", signature,
                        "X-Request-ScreenName", screenName
                );
            }

            private String generateSignature(String nonce, String timestamp, String method, String path, String secret) {
                try {
                    // Combine request fields to form string to sign
                    String stringToSign = timestamp + "|" + method + "|" + nonce + "|" + path + "|" + secret;

                    // Perform a sample cryptographic signature hash
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] hashBytes = digest.digest(stringToSign.getBytes(StandardCharsets.UTF_8));
                    return Base64.getEncoder().encodeToString(hashBytes);
                } catch (NoSuchAlgorithmException e) {
                    logger.error("Hashing algorithm not found", e);
                    return "signature-failed";
                }
            }
        }
}

