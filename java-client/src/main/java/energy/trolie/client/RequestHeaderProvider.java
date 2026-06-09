package energy.trolie.client;

import java.util.Map;

/**
 * Interface for providing dynamic request headers (e.g., Auth tokens, nonces).
 *
 * <p><strong>Implementation Note:</strong> Implementations must generate
 * fresh values (e.g., new nonces, current timestamps) on every call. The
 * underlying {@code HttpClient} may reuse request objects during automatic
 * retries. Do not cache values within the provider, as the SDK cannot
 * guarantee that this method will be re-invoked for every individual
 * transmission attempt.</p>
 */
public interface RequestHeaderProvider {

    Map<String, String> headersFor(TrolieRequestContext requestContext);
}
