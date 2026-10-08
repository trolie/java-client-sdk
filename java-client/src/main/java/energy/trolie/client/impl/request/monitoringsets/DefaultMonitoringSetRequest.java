package energy.trolie.client.impl.request.monitoringsets;

import energy.trolie.client.RequestHeaderProvider;
import energy.trolie.client.TrolieApiConstants;
import energy.trolie.client.TrolieHost;
import energy.trolie.client.impl.request.AbstractStreamingGet;
import energy.trolie.client.request.monitoringsets.MonitoringSetsReceiver;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * On-demand GET request for forecast limits with no ETAG usage
 */
public class DefaultMonitoringSetRequest extends AbstractStreamingGet<MonitoringSetsReceiver> {


	public DefaultMonitoringSetRequest(
			HttpClient httpClient, 
			TrolieHost host,
			RequestConfig requestConfig,
			int bufferSize, 
			JsonMapper jsonMapper,
			Map<String, String> httpHeaders,
			List<RequestHeaderProvider> providers,
			MonitoringSetsReceiver receiver) {
		
		super(httpClient, host, requestConfig, bufferSize, jsonMapper, httpHeaders, providers, receiver);
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
		return new MonitoringSetsResponseParser(receiver).parseResponse(inputStream, jsonMapper);
	}

	
}
