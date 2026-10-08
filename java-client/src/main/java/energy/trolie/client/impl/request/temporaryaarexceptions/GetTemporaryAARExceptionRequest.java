package energy.trolie.client.impl.request.temporaryaarexceptions;

import energy.trolie.client.RequestHeaderProvider;
import energy.trolie.client.TrolieApiConstants;
import energy.trolie.client.TrolieHost;
import energy.trolie.client.model.temporaryaarexceptions.TemporaryAARException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * {@code GET /temporary-aar-exceptions/{id}}.
 */
public class GetTemporaryAARExceptionRequest extends AbstractTemporaryAARExceptionRequest<TemporaryAARException> {

	private final String id;

	public GetTemporaryAARExceptionRequest(
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
		HttpGet get = new HttpGet(getFullPath());
		appendPath(get, id);
		get.addHeader(HttpHeaders.ACCEPT, TrolieApiConstants.CONTENT_TYPE_TEMPORARY_AAR_EXCEPTION);
		return get;
	}

	@Override
	protected TemporaryAARException handleEntity(HttpEntity entity) throws IOException {
		return jsonMapper.readValue(entity.getContent(), TemporaryAARException.class);
	}

}
