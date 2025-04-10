package energy.trolie.client.impl.request.operatingsnapshots;

import com.fasterxml.jackson.databind.ObjectMapper;
import energy.trolie.client.TrolieApiConstants;
import energy.trolie.client.TrolieHost;
import energy.trolie.client.impl.request.AbstractStreamingGet;
import energy.trolie.client.request.operatingsnapshots.ForecastSnapshotReceiver;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.net.URIBuilder;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Map;

/**
 * On-demand GET request for forecast limits with no ETAG usage
 */
public class ForecastSnapshotRequest extends AbstractStreamingGet<ForecastSnapshotReceiver> {

	String monitoringSet;
	Instant offsetPeriodStart;
	Instant periodEnd;
	String resourceId;
	
	public ForecastSnapshotRequest(
			HttpClient httpClient, 
			TrolieHost host,
			RequestConfig requestConfig,
			int bufferSize, 
			ObjectMapper objectMapper,
			Map<String, String> httpHeaders,
			ForecastSnapshotReceiver receiver,
			String monitoringSet,
			String resourceId,
			Instant offsetPeriodStart,
			Instant periodEnd) {
		
		super(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders, receiver);
		this.monitoringSet = monitoringSet;
		this.resourceId = resourceId;
		this.offsetPeriodStart = offsetPeriodStart;
		this.periodEnd = periodEnd;
	}

	@Override
	protected String getPath() {
		return TrolieApiConstants.PATH_FORECAST_SNAPSHOT;
	}
	
	@Override
	protected String getContentType() {
		return TrolieApiConstants.CONTENT_TYPE_FORECAST_SNAPSHOT;
	}
	
	@Override
	protected HttpGet createRequest() throws URISyntaxException {
		
		HttpGet get = super.createRequest();
		URIBuilder uriBuilder = new URIBuilder(get.getUri());
		
		if (monitoringSet != null) {
		
			//add the monitoring set parameter to the base URI
			uriBuilder.addParameter(TrolieApiConstants.PARAM_MONITORING_SET, monitoringSet);
		}

		if (resourceId != null) {
			//add the resource ID parameter to the base URI
			uriBuilder.addParameter(TrolieApiConstants.PARAM_RESOURCE_ID, resourceId);
		}
		
		if (offsetPeriodStart != null) {

			//add the offset-period-start parameter to the base URI
			uriBuilder.addParameter(TrolieApiConstants.PARAM_OFFSET_PERIOD_START, offsetPeriodStart.toString());
		}

		if (periodEnd != null) {

			//add the period-end parameter to the base URI
			uriBuilder.addParameter(TrolieApiConstants.PARAM_PERIOD_END, periodEnd.toString());
		}

		get.setUri(uriBuilder.build());
		return get;
	}

	@Override
	protected Boolean handleResponseContent(InputStream inputStream) {
		return new ForecastSnapshotResponseParser(receiver).parseResponse(inputStream, jsonFactory);
	}

	
}
