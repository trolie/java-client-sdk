package org.trolie.client.request.operatingsnapshots;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trolie.client.etag.ETagStore;
import org.trolie.client.model.operatingsnapshots.ForecastPeriodSnapshot;
import org.trolie.client.model.operatingsnapshots.ForecastSnapshotHeader;
import org.trolie.client.request.streaming.AbstractStreamingSubscribedGet;
import org.trolie.client.request.streaming.exception.StreamingGetConnectionException;
import org.trolie.client.request.streaming.exception.StreamingGetHandlingException;
import org.trolie.client.util.TrolieApiConstants;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * subscription for forecast rating snapshots 
 */
public class ForecastSnapshotSubscribedRequest extends AbstractStreamingSubscribedGet<ForecastSnapshotReceiver> {

	private static final Logger logger = LoggerFactory.getLogger(ForecastSnapshotSubscribedRequest.class); 
	
	JsonFactory jsonFactory;
	String monitoringSet;
	
	public ForecastSnapshotSubscribedRequest(
			HttpClient httpClient, 
			HttpHost host, 
			RequestConfig requestConfig,
			int bufferSize, 
			ThreadPoolExecutor threadPoolExecutor, 
			ObjectMapper objectMapper, 
			int pollingRateMillis,
			ForecastSnapshotReceiver receiver,
			ETagStore eTagStore,
			String monitoringSet) {
		
		super(httpClient, host, requestConfig, bufferSize, objectMapper, pollingRateMillis, receiver, eTagStore);
		this.jsonFactory = new JsonFactory(objectMapper);
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
	protected void handleResponseContent(InputStream inputStream) {
		
		try (JsonParser parser = jsonFactory.createParser(inputStream);) {
		
			receiver.beginSnapshot();
			
			//START_OBJECT forecast
			parser.nextToken(); 
			//FIELD_NAME snapshot-header
			parser.nextToken();
			//START_OBJECT header			
			parser.nextToken();
			
			//read header
			ForecastSnapshotHeader header = parser.readValueAs(ForecastSnapshotHeader.class);
			receiver.header(header);
			
			//FIELD_NAME ratings
			parser.nextToken();
			//START_ARRAY
			parser.nextToken();
			
			//for each rating
			while (parser.nextToken() == JsonToken.START_OBJECT ) {
				
				//FIELD_NAME resource-id
				parser.nextToken();
				//resource-id
				parser.nextToken();
				
				String id = parser.getValueAsString();
				receiver.beginResource(id);

				//FIELD_NAME periods
				parser.nextToken();
				//START_ARRAY
				parser.nextToken();
				
				//for each period
				while (parser.nextToken() == JsonToken.START_OBJECT ) {
					ForecastPeriodSnapshot period = parser.readValueAs(ForecastPeriodSnapshot.class);
					receiver.period(period);
				}
				//exit loop on END_ARRAY periods
				
				//END_OBJECT rating
				parser.nextToken();
				receiver.endResource();
				
				//next token will be START_OBJECT | END_ARRAY ratings
				
			}
			//exit loop on END_ARRAY ratings
			
			receiver.endSnapshot();
			
		} catch (IOException e) {
			logger.error("I/O error handling response",e);
			receiver.error(new StreamingGetConnectionException(e));
		} catch (Exception e) {
			logger.error("Error handling response data",e);
			receiver.error(new StreamingGetHandlingException(e));
		}
		
	}
	
}
