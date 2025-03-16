package energy.trolie.client.impl.request.operatingsnapshots;

import com.fasterxml.jackson.databind.ObjectMapper;
import energy.trolie.client.impl.request.AbstractStreamingSubscribedGet;
import energy.trolie.client.request.operatingsnapshots.RealTimeSnapshotSubscribedReceiver;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.net.URIBuilder;
import energy.trolie.client.ETagStore;
import energy.trolie.client.TrolieApiConstants;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * subscribed request for real-time rating snapshots
 */
public class RealTimeSnapshotSubscribedRequest extends AbstractStreamingSubscribedGet<RealTimeSnapshotSubscribedReceiver> {
	
	String monitoringSet;
	String resourceId;
	
	public RealTimeSnapshotSubscribedRequest(
			HttpClient httpClient, 
			HttpHost host, 
			RequestConfig requestConfig,
			int bufferSize, 
			ObjectMapper objectMapper,
			Map<String, String> httpHeaders,
			int pollingRateMillis,
			RealTimeSnapshotSubscribedReceiver receiver,
			ETagStore eTagStore,
			String monitoringSet,
			String resourceId) {
		
		super(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders,
				pollingRateMillis, receiver, eTagStore);
		this.monitoringSet = monitoringSet;
		this.resourceId = resourceId;
	}

	@Override
	protected String getPath() {
		return TrolieApiConstants.PATH_REALTIME_SNAPSHOT;
	}
	
	@Override
	protected String getContentType() {
		return TrolieApiConstants.CONTENT_TYPE_REALTIME_SNAPSHOT;
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
		
		if (resourceId != null) {
			
			//add the transmission facility parameter to the base URI
			URIBuilder uriBuilder = new URIBuilder(get.getUri())
					.addParameter(TrolieApiConstants.PARAM_RESOURCE_ID, resourceId);
			get.setUri(uriBuilder.build());
		}
		
		return get;
	}
	
	@Override
	protected Boolean handleResponseContent(InputStream inputStream) {
		
		return new RealTimeSnapshotResponseParser(receiver).parseResponse(inputStream, jsonFactory);
		
	}
	
}
