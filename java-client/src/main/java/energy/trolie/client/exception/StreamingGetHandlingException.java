package energy.trolie.client.exception;

/**
 * Exception indicating an issue with interpreting the response bytes
 */
public class StreamingGetHandlingException extends StreamingGetException {

	/**
	 *
	 * @param message message
	 * @param cause parent
	 */
	public StreamingGetHandlingException(String message, Throwable cause) {
		super(cause);
	}

	/**
	 *
	 * @param message message
	 */
	public StreamingGetHandlingException(String message) {
		super(message);
	}

	/**
	 *
	 * @param cause parent
	 */
	public StreamingGetHandlingException(Throwable cause) {
		super(cause);
	}
	
}
