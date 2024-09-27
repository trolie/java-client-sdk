package org.trolie.client.request.operatingsnapshots;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trolie.client.model.operatingsnapshots.RealTimeLimit;
import org.trolie.client.model.operatingsnapshots.RealTimeSnapshotHeader;
import org.trolie.client.request.streaming.exception.StreamingGetConnectionException;
import org.trolie.client.request.streaming.exception.StreamingGetHandlingException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import lombok.AllArgsConstructor;

/**
 * Implementation for parsing a real-time snapshot response shared between subscribed and on-demand requests
 */
@AllArgsConstructor
public class RealTimeSnapshotResponseParser {

	private static final Logger logger = LoggerFactory.getLogger(RealTimeSnapshotResponseParser.class);
	
	RealTimeSnapshotReceiver receiver;

	public void parseResponse(InputStream inputStream, JsonFactory jsonFactory) {
		
		try (JsonParser parser = jsonFactory.createParser(inputStream);) {
			
			receiver.beginSnapshot();
			
			//START_OBJECT forecast
			parser.nextToken(); 
			//FIELD_NAME snapshot-header
			parser.nextToken();
			//START_OBJECT header			
			parser.nextToken();
			
			//read header
			RealTimeSnapshotHeader header = parser.readValueAs(RealTimeSnapshotHeader.class);
			receiver.header(header);
			
			//FIELD_NAME ratings
			parser.nextToken();
			//START_ARRAY
			parser.nextToken();
			
			//for each limit
			while (parser.nextToken() == JsonToken.START_OBJECT ) {
				
				RealTimeLimit limit = parser.readValueAs(RealTimeLimit.class);
				receiver.limit(limit);
				
			}
			//exit loop on END_ARRAY limits
			
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
