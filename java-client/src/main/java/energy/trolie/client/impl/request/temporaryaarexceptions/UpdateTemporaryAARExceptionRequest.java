package energy.trolie.client.impl.request.temporaryaarexceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import energy.trolie.client.RequestHeaderProvider;
import energy.trolie.client.TrolieApiConstants;
import energy.trolie.client.TrolieHost;
import energy.trolie.client.model.temporaryaarexceptions.TemporaryAARExceptionRequest;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * <p>{@code PUT /temporary-aar-exceptions/{id}}.</p>
 * <p>Also used to represent termination of a Temporary AAR Exception: TROLIE does not
 * define a separate terminate operation, so termination is simply an update with an
 * earlier {@code end-time}.</p>
 * <p>Returns no content on success (HTTP 204).</p>
 */
public class UpdateTemporaryAARExceptionRequest extends AbstractTemporaryAARExceptionRequest<Void> {

	private final String id;
	private final TemporaryAARExceptionRequest requestBody;

	public UpdateTemporaryAARExceptionRequest(
			HttpClient httpClient,
			TrolieHost host,
			RequestConfig requestConfig,
			ObjectMapper objectMapper,
			Map<String, String> httpHeaders,
			List<RequestHeaderProvider> providers,
			String id,
			TemporaryAARExceptionRequest requestBody) {
		super(httpClient, host, requestConfig, objectMapper, httpHeaders, providers);
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Temporary AAR Exception id cannot be null or blank");
		}
		this.id = id;
		this.requestBody = requestBody;
	}

	@Override
	protected HttpUriRequestBase createRequest() throws URISyntaxException, IOException {
		HttpPut put = new HttpPut(getFullPath());
		appendPath(put, id);
		String json = objectMapper.writeValueAsString(requestBody);
		put.setEntity(new StringEntity(json, ContentType.create(TrolieApiConstants.CONTENT_TYPE_TEMPORARY_AAR_EXCEPTION)));
		put.addHeader(HttpHeaders.CONTENT_TYPE, TrolieApiConstants.CONTENT_TYPE_TEMPORARY_AAR_EXCEPTION);
		return put;
	}

}
