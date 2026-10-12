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
import org.apache.hc.core5.net.URIBuilder;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * {@code GET /temporary-aar-exceptions}, optionally filtered by operating period,
 * segment, and/or monitoring set.
 */
public class GetTemporaryAARExceptionsRequest extends AbstractTemporaryAARExceptionRequest<List<TemporaryAARException>> {

	private final Instant periodStart;
	private final Instant periodEnd;
	private final String segment;
	private final String monitoringSet;

	public GetTemporaryAARExceptionsRequest(
			HttpClient httpClient,
			TrolieHost host,
			RequestConfig requestConfig,
			JsonMapper jsonMapper,
			Map<String, String> httpHeaders,
			List<RequestHeaderProvider> providers,
			Instant periodStart,
			Instant periodEnd,
			String segment,
			String monitoringSet) {
		super(httpClient, host, requestConfig, jsonMapper, httpHeaders, providers);
		this.periodStart = periodStart;
		this.periodEnd = periodEnd;
		this.segment = segment;
		this.monitoringSet = monitoringSet;
	}

	@Override
	protected HttpUriRequestBase createRequest() throws URISyntaxException {
		HttpGet get = new HttpGet(getFullPath());

		URIBuilder uriBuilder = new URIBuilder(get.getUri());
		if (periodStart != null) {
			uriBuilder.addParameter(TrolieApiConstants.PARAM_PERIOD_START, DateTimeFormatter.ISO_INSTANT.format(periodStart));
		}
		if (periodEnd != null) {
			uriBuilder.addParameter(TrolieApiConstants.PARAM_PERIOD_END, DateTimeFormatter.ISO_INSTANT.format(periodEnd));
		}
		if (segment != null && !segment.isBlank()) {
			uriBuilder.addParameter(TrolieApiConstants.PARAM_SEGMENT, segment);
		}
		if (monitoringSet != null && !monitoringSet.isBlank()) {
			uriBuilder.addParameter(TrolieApiConstants.PARAM_MONITORING_SET, monitoringSet);
		}
		get.setUri(uriBuilder.build());

		get.addHeader(HttpHeaders.ACCEPT, TrolieApiConstants.CONTENT_TYPE_TEMPORARY_AAR_EXCEPTION_SET);
		return get;
	}

	@Override
	protected List<TemporaryAARException> handleEntity(HttpEntity entity) throws IOException {
		var collectionType = jsonMapper.getTypeFactory()
				.constructCollectionType(List.class, TemporaryAARException.class);
		return jsonMapper.readValue(entity.getContent(), collectionType);
	}

}
