package energy.trolie.client.impl.request.temporaryaarexceptions;

import energy.trolie.client.RequestHeaderProvider;
import energy.trolie.client.TrolieHost;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import tools.jackson.databind.json.JsonMapper;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * <p>{@code DELETE /temporary-aar-exceptions/{id}}.</p>
 * <p>Returns no content on success (HTTP 204).</p>
 */
public class DeleteTemporaryAARExceptionRequest extends AbstractTemporaryAARExceptionRequest<Void> {

	private final String id;

	public DeleteTemporaryAARExceptionRequest(
			HttpClient httpClient,
			TrolieHost host,
			RequestConfig requestConfig,
			JsonMapper jsonMapper,
			Map<String, String> httpHeaders,
			List<RequestHeaderProvider> providers,
			String id) {
		super(httpClient, host, requestConfig, jsonMapper, httpHeaders, providers);
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Temporary AAR Exception id cannot be null or blank");
		}
		this.id = id;
	}

	@Override
	protected HttpUriRequestBase createRequest() throws URISyntaxException {
		HttpDelete delete = new HttpDelete(getFullPath());
		appendPath(delete, id);
		return delete;
	}

}
