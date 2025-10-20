package energy.trolie.client.impl.request.operatingsnapshots;

import com.fasterxml.jackson.databind.ObjectMapper;
import energy.trolie.client.TrolieApiConstants;
import energy.trolie.client.TrolieHost;
import energy.trolie.client.impl.request.AbstractStreamingGet;
import energy.trolie.client.request.operatingsnapshots.SeasonalSnapshotReceiver;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.net.URIBuilder;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * On-demand GET request for seasonal limits with ETAG usage
 */
public class SeasonalSnapshotRequest extends AbstractStreamingGet<SeasonalSnapshotReceiver> {

	String monitoringSet;
	String resourceId;

	public SeasonalSnapshotRequest(
			HttpClient httpClient, 
			TrolieHost host,
			RequestConfig requestConfig,
			int bufferSize, 
			ObjectMapper objectMapper,
			Map<String, String> httpHeaders,
			SeasonalSnapshotReceiver receiver, // seasonal
			String monitoringSet,
			String resourceId) {
		
		super(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders, receiver);
		this.monitoringSet = monitoringSet;
		this.resourceId = resourceId;
	}

	@Override
	protected String getPath() {
		return TrolieApiConstants.PATH_SEASONAL_SNAPSHOT;
	}
	
	@Override
	protected String getContentType() {
		return TrolieApiConstants.CONTENT_TYPE_SEASONAL_SNAPSHOT;
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

		get.setUri(uriBuilder.build());
		return get;
	}

	@Override
	protected Boolean handleResponseContent(InputStream inputStream) {
		return new SeasonalSnapshotResponseParser(receiver).parseResponse(inputStream, jsonFactory);
	}

	
}
