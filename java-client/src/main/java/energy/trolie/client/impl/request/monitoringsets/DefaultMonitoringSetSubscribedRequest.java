package energy.trolie.client.impl.request.monitoringsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import energy.trolie.client.request.monitoringsets.MonitoringSetsSubscribedReceiver;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import energy.trolie.client.ETagStore;
import energy.trolie.client.impl.request.AbstractStreamingSubscribedGet;
import energy.trolie.client.TrolieApiConstants;

import java.io.InputStream;
import java.util.Map;

/**
 * Subscribed request for default monitoring set updates
 */
public class DefaultMonitoringSetSubscribedRequest extends AbstractStreamingSubscribedGet<MonitoringSetsSubscribedReceiver> {


	public DefaultMonitoringSetSubscribedRequest(
			HttpClient httpClient, 
			HttpHost host, 
			RequestConfig requestConfig,
			int bufferSize, 
			ObjectMapper objectMapper,
			Map<String, String> httpHeaders,
			int pollingRateMillis,
			MonitoringSetsSubscribedReceiver receiver,
			ETagStore eTagStore) {
		super(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders, pollingRateMillis,
				receiver, eTagStore);
	}

	@Override
	protected String getPath() {
		return TrolieApiConstants.PATH_DEFAULT_MONITORING_SET;
	}
	
	@Override
	protected String getContentType() {
		return TrolieApiConstants.CONTENT_TYPE_MONITORING_SET;
	}
	
	@Override
	protected Boolean handleResponseContent(InputStream inputStream) {
		return new MonitoringSetsResponseParser(receiver).parseResponse(inputStream, jsonFactory);
	}
}
