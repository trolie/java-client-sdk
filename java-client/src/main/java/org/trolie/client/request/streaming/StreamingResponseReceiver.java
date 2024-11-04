package org.trolie.client.request.streaming;

import org.trolie.client.request.streaming.exception.StreamingGetConnectionException;
import org.trolie.client.request.streaming.exception.StreamingGetException;
import org.trolie.client.request.streaming.exception.StreamingGetHandlingException;
import org.trolie.client.request.streaming.exception.StreamingGetResponseException;

/**
 * Base interface for receiver of streaming GET response data
 */
public interface StreamingResponseReceiver {

	/**
	 * Called when an error occurs establishing, executing or handling the polling request. 
	 * Possible exception types are: 
	 * <p>{@link StreamingGetConnectionException} indicating connection or request initialization error, or an I/O error while reading response bytes
	 * <p>{@link StreamingGetResponseException} indicating a server response with an abnormal HTTP status code 
	 * <p>{@link StreamingGetHandlingException} indicating an issue with interpreting the response bytes
	 * 
	 * @param t
	 */
	default void error(StreamingGetException t) {
	}
	
}
