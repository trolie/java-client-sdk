package energy.trolie.client.impl.request.temporaryaarexceptions;

import energy.trolie.client.RequestHeaderProvider;
import energy.trolie.client.TrolieApiConstants;
import energy.trolie.client.TrolieHost;
import energy.trolie.client.TrolieRequestContext;
import energy.trolie.client.exception.TrolieException;
import energy.trolie.client.exception.TrolieServerException;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.net.URIBuilder;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Common base for the synchronous, non-streaming Temporary AAR Exception operations
 * ({@code POST}, {@code GET}, {@code PUT}, {@code DELETE} against
 * {@value TrolieApiConstants#PATH_TEMPORARY_AAR_EXCEPTIONS}).</p>
 * <p>Unlike most other TROLIE resources, Temporary AAR Exceptions are small, individually
 * addressable objects rather than large time-series payloads. So, in contrast to the
 * streaming receiver/update pattern used elsewhere in this SDK, these operations are
 * implemented as simple synchronous request/response calls that return (or accept) a
 * fully-populated in-memory object.</p>
 *
 * @param <T> the type returned by this request. Use {@link Void} for operations
 *           that return no content (i.e. HTTP 204).
 */
public abstract class AbstractTemporaryAARExceptionRequest<T> {

	protected final HttpClient httpClient;
	protected final TrolieHost host;
	protected final RequestConfig requestConfig;
	protected final JsonMapper jsonMapper;
	protected final Map<String, String> httpHeaders;
	protected final List<RequestHeaderProvider> providers;

	protected AbstractTemporaryAARExceptionRequest(
			HttpClient httpClient,
			TrolieHost host,
			RequestConfig requestConfig,
			JsonMapper jsonMapper,
			Map<String, String> httpHeaders,
			List<RequestHeaderProvider> providers) {
		this.httpClient = httpClient;
		this.host = host;
		this.requestConfig = requestConfig;
		this.jsonMapper = jsonMapper;
		this.httpHeaders = httpHeaders;
		this.providers = providers;
	}

	/**
	 * @return the base path for this request, before any identifier or query parameters are appended.
	 */
	protected String getPath() {
		return TrolieApiConstants.PATH_TEMPORARY_AAR_EXCEPTIONS;
	}

	/**
	 * Builds the Apache HTTP request for this operation, including method, path,
	 * and (if applicable) request body. Headers and request config are applied
	 * separately by {@link #execute()}.
	 *
	 * @return configured request
	 * @throws URISyntaxException if the request URI cannot be constructed
	 * @throws IOException if the request body cannot be serialized
	 */
	protected abstract HttpUriRequestBase createRequest() throws URISyntaxException, IOException;

	/**
	 * Parses a successful (2xx) response body, if one is expected.
	 * The default implementation returns {@code null} and is appropriate for
	 * operations that return no content.
	 *
	 * @param entity response entity
	 * @return parsed response
	 * @throws IOException on parsing failure
	 */
	protected T handleEntity(HttpEntity entity) throws IOException {
		return null;
	}

	/**
	 * Appends the given path segment (such as an object identifier) to the base path.
	 *
	 * @param request request whose URI should be updated
	 * @param pathSegment segment to append
	 * @throws URISyntaxException if the resulting URI is invalid
	 */
	protected void appendPath(HttpUriRequestBase request, String pathSegment) throws URISyntaxException {
		URIBuilder uriBuilder = new URIBuilder(request.getUri());
		uriBuilder.appendPath(pathSegment);
		request.setUri(uriBuilder.build());
	}

	/**
	 * @return the full path for this operation, including any base path configured on the {@link TrolieHost}.
	 */
	protected String getFullPath() {
		return host.hasBasePath() ? host.getBasePath() + getPath() : getPath();
	}

	private void applyRequestHeaderProviders(HttpUriRequestBase request) throws URISyntaxException {
		var header = request.getFirstHeader(HttpHeaders.CONTENT_TYPE);
		String contentType = header != null ? header.getValue() : null;

		TrolieRequestContext context = new TrolieRequestContext(request.getMethod(), request.getUri(), contentType);

		var mergedHeaders = new LinkedHashMap<String, String>();
		for (var provider : providers) {
			mergedHeaders.putAll(provider.headersFor(context));
		}

		mergedHeaders.forEach(request::setHeader);
	}

	/**
	 * Executes the request synchronously.
	 *
	 * @return the parsed response, as defined by {@link #handleEntity(HttpEntity)}.
	 * Will be {@code null} for operations that return no content.
	 */
	public T execute() {
		try {
			HttpUriRequestBase request = createRequest();

			if (httpHeaders != null && !httpHeaders.isEmpty()) {
				httpHeaders.forEach(request::addHeader);
			}
			request.setConfig(requestConfig);

			if (providers != null && !providers.isEmpty()) {
				applyRequestHeaderProviders(request);
			}

			return httpClient.execute(host.getHost(), request, new AbstractHttpClientResponseHandler<>() {
				@Override
				public T handleEntity(HttpEntity entity) throws IOException {
					return AbstractTemporaryAARExceptionRequest.this.handleEntity(entity);
				}
			});
		} catch (HttpResponseException e) {
			throw new TrolieServerException(e.getStatusCode(),
					"Trolie server returned error status code " + e.getStatusCode(), e);
		} catch (IOException e) {
			throw new TrolieException("I/O error executing request", e);
		} catch (URISyntaxException e) {
			throw new TrolieException("Error constructing request URI", e);
		}
	}

}
