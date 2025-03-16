package energy.trolie.client;

import energy.trolie.client.exception.StreamingGetConnectionException;
import energy.trolie.client.exception.StreamingGetException;
import energy.trolie.client.exception.StreamingGetHandlingException;
import energy.trolie.client.exception.StreamingGetResponseException;

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
	 * @param t received exception
	 */
	default void error(StreamingGetException t) {
	}
	
}
