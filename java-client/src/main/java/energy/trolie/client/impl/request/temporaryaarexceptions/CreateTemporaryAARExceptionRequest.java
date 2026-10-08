package energy.trolie.client.impl.request.temporaryaarexceptions;

import energy.trolie.client.RequestHeaderProvider;
import energy.trolie.client.TrolieApiConstants;
import energy.trolie.client.TrolieHost;
import energy.trolie.client.model.temporaryaarexceptions.TemporaryAARException;
import energy.trolie.client.model.temporaryaarexceptions.TemporaryAARExceptionRequest;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * {@code POST /temporary-aar-exceptions}.
 */
public class CreateTemporaryAARExceptionRequest extends AbstractTemporaryAARExceptionRequest<TemporaryAARException> {

	private final TemporaryAARExceptionRequest requestBody;

	public CreateTemporaryAARExceptionRequest(
			HttpClient httpClient,
			TrolieHost host,
			RequestConfig requestConfig,
			JsonMapper jsonMapper,
			Map<String, String> httpHeaders,
			List<RequestHeaderProvider> providers,
			TemporaryAARExceptionRequest requestBody) {
		super(httpClient, host, requestConfig, jsonMapper, httpHeaders, providers);
		this.requestBody = requestBody;
	}

	@Override
	protected HttpUriRequestBase createRequest() throws IOException {
		HttpPost post = new HttpPost(getFullPath());
		String json = jsonMapper.writeValueAsString(requestBody);
		post.setEntity(new StringEntity(json, ContentType.create(TrolieApiConstants.CONTENT_TYPE_TEMPORARY_AAR_EXCEPTION)));
		post.addHeader(HttpHeaders.CONTENT_TYPE, TrolieApiConstants.CONTENT_TYPE_TEMPORARY_AAR_EXCEPTION);
		post.addHeader(HttpHeaders.ACCEPT, TrolieApiConstants.CONTENT_TYPE_TEMPORARY_AAR_EXCEPTION);
		return post;
	}

	@Override
	protected TemporaryAARException handleEntity(HttpEntity entity) throws IOException {
		return jsonMapper.readValue(entity.getContent(), TemporaryAARException.class);
	}

}
