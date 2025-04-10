package energy.trolie.client;

import energy.trolie.client.exception.TrolieException;
import lombok.Getter;
import org.apache.hc.core5.http.HttpHost;

import java.net.URI;
import java.net.URISyntaxException;

@Getter
public class TrolieHost {

    private final HttpHost host;
    private final String basePath;

    public TrolieHost(String baseUrl) {
        try {
            URI uri = new URI(baseUrl);
            this.host = HttpHost.create(uri);
            this.basePath = uri.getPath();
        } catch (URISyntaxException e) {
            throw new TrolieException(e);
        }
    }

    public boolean hasBasePath() {
        return this.basePath != null;
    }
}
