package org.trolie.client.request.operatingsnapshots;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.net.URIBuilder;
import org.trolie.client.etag.ETagStore;
import org.trolie.client.request.streaming.AbstractStreamingSubscribedGet;
import org.trolie.client.util.TrolieApiConstants;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * subscribed request for real-time rating snapshots
 */
public class RealTimeSnapshotSubscribedRequest extends AbstractStreamingSubscribedGet<RealTimeSnapshotSubscribedReceiver> {
	
	JsonFactory jsonFactory;
	String monitoringSet;
	String transmissionFacility;
	
	public RealTimeSnapshotSubscribedRequest(
			HttpClient httpClient, 
			HttpHost host, 
			RequestConfig requestConfig,
			int bufferSize, 
			ThreadPoolExecutor threadPoolExecutor, 
			ObjectMapper objectMapper, 
			int pollingRateMillis,
			RealTimeSnapshotSubscribedReceiver receiver,
			ETagStore eTagStore,
			String monitoringSet,
			String transmissionFacility) {
		
		super(httpClient, host, requestConfig, bufferSize, objectMapper, pollingRateMillis, receiver, eTagStore);
		this.jsonFactory = new JsonFactory(objectMapper);
		this.monitoringSet = monitoringSet;
		this.transmissionFacility = transmissionFacility;
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
		
		if (monitoringSet != null) {
			
			//add the transmission facility parameter to the base URI
			URIBuilder uriBuilder = new URIBuilder(get.getUri())
					.addParameter(TrolieApiConstants.PARAM_TRANSMISSION_FACILITY, transmissionFacility);
			get.setUri(uriBuilder.build());
		}
		
		return get;
	}
	
	@Override
	protected Boolean handleResponseContent(InputStream inputStream) {
		
		return new RealTimeSnapshotResponseParser(receiver).parseResponse(inputStream, jsonFactory);
		
	}
	
}
