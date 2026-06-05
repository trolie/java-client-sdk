package energy.trolie.client;

import java.util.Map;

public interface RequestHeaderProvider {

    Map<String, String> headersFor(TrolieRequestContext requestContext);
}
