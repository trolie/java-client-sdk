package energy.trolie.client.impl.request.monitoringsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import energy.trolie.client.ETagStore;
import energy.trolie.client.TrolieApiConstants;
import energy.trolie.client.TrolieHost;
import energy.trolie.client.impl.request.AbstractStreamingSubscribedGet;
import energy.trolie.client.request.monitoringsets.MonitoringSetsSubscribedReceiver;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.net.URIBuilder;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Subscribed request for monitoring set updates
 */
public class MonitoringSetsSubscribedRequest extends AbstractStreamingSubscribedGet<MonitoringSetsSubscribedReceiver> {

	String monitoringSet;
	
	public MonitoringSetsSubscribedRequest(
			HttpClient httpClient, 
			TrolieHost host,
			RequestConfig requestConfig,
			int bufferSize, 
			ObjectMapper objectMapper,
			Map<String, String> httpHeaders,
			int pollingRateMillis,
			MonitoringSetsSubscribedReceiver receiver,
			ETagStore eTagStore,
			String monitoringSet) {
		
		super(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders, pollingRateMillis,
				receiver, eTagStore);

		if (monitoringSet == null || monitoringSet.isBlank()) {
			throw new IllegalArgumentException("Monitoring set name cannot be null or blank");
		}
		this.monitoringSet = monitoringSet;
	}

	@Override
	protected String getPath() {
		return TrolieApiConstants.PATH_MONITORING_SET_ID;
	}
	
	@Override
	protected String getContentType() {
		return TrolieApiConstants.CONTENT_TYPE_MONITORING_SET;
	}

	@Override
	protected HttpGet createRequest() throws URISyntaxException {
		HttpGet get = super.createRequest();
		//add the monitoring set parameter to the base URI
		URIBuilder uriBuilder = new URIBuilder(get.getUri());
		uriBuilder.appendPath(monitoringSet);
		get.setUri(uriBuilder.build());
		return get;
	}
	
	@Override
	protected Boolean handleResponseContent(InputStream inputStream) {
		
		return new MonitoringSetsResponseParser(receiver).parseResponse(inputStream, jsonFactory);
		
	}
	
}
