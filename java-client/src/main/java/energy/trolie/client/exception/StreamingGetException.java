package energy.trolie.client.exception;

import energy.trolie.client.StreamingResponseReceiver;

/**
 * Common base for exceptions received in
 * {@link StreamingResponseReceiver#error(StreamingGetException)}
 */
public abstract class StreamingGetException extends TrolieException {

	/**
	 *
	 * @param cause parent
	 */
	protected StreamingGetException(Throwable cause) {
		super(cause);
	}

	/**
	 *
	 * @param message message
	 * @param cause parent
	 */
	protected StreamingGetException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 *
	 * @param message message
	 */
	protected StreamingGetException(String message) {
		super(message);
	}
	
}
