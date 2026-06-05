package energy.trolie.client;

import java.net.URI;

public record TrolieRequestContext(String method, URI uri, String contentType) {
}


