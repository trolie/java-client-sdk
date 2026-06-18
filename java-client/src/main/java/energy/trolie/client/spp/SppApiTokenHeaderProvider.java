package energy.trolie.client.spp;

import energy.trolie.client.RequestHeaderProvider;
import energy.trolie.client.TrolieRequestContext;
import lombok.extern.slf4j.Slf4j;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * A {@link RequestHeaderProvider} that implements the SPP Two-Factor Authentication
 * (TFA) token generation as specified in the SPP TFA Technical Specifications v1.4.
 * <p>
 * For every outgoing request, this provider generates a fresh {@code X-SPP-API-Token}
 * header containing:
 * <ul>
 *   <li>A UTC timestamp</li>
 *   <li>A unique nonce (UUID, max 128 characters)</li>
 *   <li>An HMAC-SHA512 signature over the request metadata</li>
 * </ul>
 */

@Slf4j
public final class SppApiTokenHeaderProvider implements RequestHeaderProvider {

    private static final String HEADER_NAME = "X-SPP-API-Token";


    private final String screenName;
    private final byte[] decodedApiKey;
    private final Clock clock;

    /**
     * Creates an {@link SppApiTokenHeaderProvider} with the given credentials.
     *
     * @param screenName the SPP-assigned screen name for this client
     * @param apiKey     the Base64-encoded API key assigned by SPP
     * @param clock      the clock to use for timestamp generation (use {@link Clock#systemUTC()} in production)
     */
    public SppApiTokenHeaderProvider(String screenName, String apiKey, Clock clock) {
        this.screenName = screenName;
        this.decodedApiKey = Base64.getDecoder().decode(apiKey);
        this.clock = clock;
    }

    @Override
    public Map<String, String> headersFor(TrolieRequestContext context) {
        // 1. Generate timestamp (truncated to seconds, ISO-8601)
        String timestamp = DateTimeFormatter.ISO_INSTANT
                .format(clock.instant().truncatedTo(ChronoUnit.SECONDS));

        // 2. Generate unique nonce (UUID satisfies the 128-char max requirement)
        String nonce = UUID.randomUUID().toString();

        // 3. Lowercase the screen name and request path (as per TFA spec)
        String lowerScreenName = screenName.toLowerCase(Locale.ROOT);
        String lowerPath = context.uri().getPath().toLowerCase(Locale.ROOT);

        // 4. Build the string to sign
        String stringToSign = nonce + timestamp + lowerScreenName + lowerPath;
        log.debug("Creating SPP API token with string to sign: {}", stringToSign);

        // 5. Compute HMAC-SHA512 and Base64-encode
        byte[] hmacBytes;
        try{
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(decodedApiKey, "HmacSHA512");
            mac.init(secretKeySpec);
            hmacBytes = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        var hmacHash = Base64.getEncoder().encodeToString(hmacBytes);

        log.debug("Generated HMAC hash: {}", hmacHash);

        // 6. Assemble the final token
        String token = timestamp + "-" + nonce + "-" + hmacHash;

        return Map.of(HEADER_NAME, token);
    }

}