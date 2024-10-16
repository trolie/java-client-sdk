package org.trolie.client.request.operatingsnapshots;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.net.URIBuilder;
import org.trolie.client.request.streaming.AbstractStreamingGet;
import org.trolie.client.util.TrolieApiConstants;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * On-demand GET request for real-time limits with no ETAG usage
 */
public class ForecastSnapshotRequest extends AbstractStreamingGet<ForecastSnapshotReceiver> {

	JsonFactory jsonFactory;
	String monitoringSet;
	Instant periodStart;
	Instant periodEnd;
	String transmissionFacility;
	
	public ForecastSnapshotRequest(
			HttpClient httpClient, 
			HttpHost host, 
			RequestConfig requestConfig,
			int bufferSize, 
			ThreadPoolExecutor threadPoolExecutor, 
			ObjectMapper objectMapper, 
			ForecastSnapshotReceiver receiver,
			String monitoringSet,
			Instant periodStart,
			Instant periodEnd) {
		
		super(httpClient, host, requestConfig, bufferSize, objectMapper, receiver);
		this.jsonFactory = new JsonFactory(objectMapper);
		this.monitoringSet = monitoringSet;
		this.periodStart = periodStart;
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
		
		if (monitoringSet != null) {
		
			//add the monitoring set parameter to the base URI
			URIBuilder uriBuilder = new URIBuilder(get.getUri())
					.addParameter(TrolieApiConstants.PARAM_MONITORING_SET, monitoringSet);
			get.setUri(uriBuilder.build());
		}
		
		if (periodStart != null) {

			//add the period-start or offset-period-start parameter to the base URI
			URIBuilder uriBuilder = new URIBuilder(get.getUri())
					.addParameter(
							//if no period-end is given, assume that the date is being used as an offset
							periodEnd != null ? TrolieApiConstants.PARAM_PERIOD_START : TrolieApiConstants.PARAM_OFFSET_PERIOD_START, 
							periodStart.toString());
			get.setUri(uriBuilder.build());
		}

		if (periodEnd != null) {

			//add the period-end or offset-period-end parameter to the base URI
			URIBuilder uriBuilder = new URIBuilder(get.getUri())
					.addParameter(
							//if no period-start is given, assume that the date is being used as an offset
							periodStart != null ? TrolieApiConstants.PARAM_PERIOD_END : TrolieApiConstants.PARAM_OFFSET_PERIOD_END, 
							periodEnd.toString());
			get.setUri(uriBuilder.build());
		}
		
		return get;
	}

	@Override
	protected void handleResponseContent(InputStream inputStream) {
		new ForecastSnapshotResponseParser(receiver).parseResponse(inputStream, jsonFactory);
	}

	
}
