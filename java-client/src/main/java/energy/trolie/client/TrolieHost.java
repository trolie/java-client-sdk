package energy.trolie.client;

import energy.trolie.client.exception.TrolieException;
import lombok.Getter;
import org.apache.hc.core5.http.HttpHost;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Holds all details needed to describe an HTTP connection to a TROLIE host. It is wrapper around HttpHost that
 * additionally allows for a common path for all TROLIE endpoints.
 */
@Getter
public class TrolieHost {

    /**
     * HttpHost describing the host name, port, and protocol scheme of the TROLIE host URL.
     */
    private final HttpHost host;

    /**
     * An optional base path that applies to all TROLIE endpoints.
     */
    private final String basePath;

    /**
     * Create a TrolieHost from the base URL of the TROLIE server. A base path can optionally be included in the URL
     * and will apply to all endpoints. For example, https://hostname and https://hostname/trolie-path are both valid.
     * @param baseUrl TROLIE server URL
     */
    public TrolieHost(String baseUrl) {
        try {
            URI uri = new URI(baseUrl);
            this.host = HttpHost.create(uri);
            this.basePath = uri.getPath();
        } catch (URISyntaxException e) {
            throw new TrolieException(e);
        }
    }

    /**
     * Returns true if the TROLIE server URL includes a base path
     * @return true if a base path is present
     */
    public boolean hasBasePath() {
        return this.basePath != null;
    }
}
