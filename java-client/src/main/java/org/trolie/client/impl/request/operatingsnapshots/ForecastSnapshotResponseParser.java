package org.trolie.client.impl.request.operatingsnapshots;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trolie.client.model.operatingsnapshots.ForecastPeriodSnapshot;
import org.trolie.client.model.operatingsnapshots.ForecastSnapshotHeader;
import org.trolie.client.exception.StreamingGetConnectionException;
import org.trolie.client.exception.StreamingGetHandlingException;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotReceiver;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation for parsing a real-time snapshot response shared between subscribed and on-demand requests
 */
@AllArgsConstructor
public class ForecastSnapshotResponseParser {

	private static final Logger logger = LoggerFactory.getLogger(ForecastSnapshotResponseParser.class);
	
	ForecastSnapshotReceiver receiver;

	public Boolean parseResponse(InputStream inputStream, JsonFactory jsonFactory) {
		
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
			return true;

		} catch (IOException e) {
			logger.error("I/O error handling response",e);
			receiver.error(new StreamingGetConnectionException(e));
		} catch (Exception e) {
			logger.error("Error handling response data",e);
			receiver.error(new StreamingGetHandlingException(e));
		}

		return false;
	}
	
}
