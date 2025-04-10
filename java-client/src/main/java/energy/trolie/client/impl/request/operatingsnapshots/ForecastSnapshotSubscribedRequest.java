package energy.trolie.client.impl.request.operatingsnapshots;

import com.fasterxml.jackson.databind.ObjectMapper;
import energy.trolie.client.ETagStore;
import energy.trolie.client.TrolieApiConstants;
import energy.trolie.client.TrolieHost;
import energy.trolie.client.impl.request.AbstractStreamingSubscribedGet;
import energy.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscribedReceiver;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.net.URIBuilder;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * subscription for forecast rating snapshots 
 */
public class ForecastSnapshotSubscribedRequest extends AbstractStreamingSubscribedGet<ForecastSnapshotSubscribedReceiver> {

	String monitoringSet;
	
	public ForecastSnapshotSubscribedRequest(
			HttpClient httpClient, 
			TrolieHost host,
			RequestConfig requestConfig,
			int bufferSize, 
			ObjectMapper objectMapper,
			Map<String, String> httpHeaders,
			int pollingRateMillis,
			ForecastSnapshotSubscribedReceiver receiver,
			ETagStore eTagStore,
			String monitoringSet) {
		
		super(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders, pollingRateMillis,
				receiver, eTagStore);
		this.monitoringSet = monitoringSet;
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
		
		return get;
	}
	
	@Override
	protected Boolean handleResponseContent(InputStream inputStream) {
		
		return new ForecastSnapshotResponseParser(receiver).parseResponse(inputStream, jsonFactory);
		
	}
	
}
