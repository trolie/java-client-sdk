package org.trolie.client.request.operatingsnapshots;

import java.io.InputStream;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.trolie.client.model.operatingsnapshots.ForecastPeriodSnapshot;
import org.trolie.client.model.operatingsnapshots.ForecastSnapshotHeader;
import org.trolie.client.request.streaming.AbstractStreamingGetSubscription;
import org.trolie.client.request.streaming.SubscriberRequestHandlingException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * subscription for forecast rating snapshots 
 */
public class ForecastSnapshotSubscription extends AbstractStreamingGetSubscription<ForecastSnapshotStreamingReceiver> {

	public static final String PATH = "/limits/forecast-snapshot";
	public static final String CONTENT_TYPE = "application/vnd.trolie.forecast-limits-snapshot.v1+json";
	
	JsonFactory jsonFactory;
	
	public ForecastSnapshotSubscription(
			HttpClient httpClient, 
			HttpHost host, 
			RequestConfig requestConfig,
			int bufferSize, 
			ThreadPoolExecutor threadPoolExecutor, 
			ObjectMapper objectMapper, 
			int pollingRateMillis,
			ForecastSnapshotStreamingReceiver receiver) {
		
		super(httpClient, host, requestConfig, bufferSize, objectMapper, pollingRateMillis, receiver);
		this.jsonFactory = new JsonFactory(objectMapper);
	}

	@Override
	protected String getPath() {
		return PATH;
	}
	
	@Override
	protected String getContentType() {
		return CONTENT_TYPE;
	}
	
	@Override
	protected void handleNewContent(InputStream inputStream) throws Exception {
		
		
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
			
		} catch (Exception e) {
			receiver.error(new SubscriberRequestHandlingException(e));
		}
		
	}
	
}
