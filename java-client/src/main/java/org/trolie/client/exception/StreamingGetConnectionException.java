package org.trolie.client.exception;

/**
 * Exception indicating connection or request
 * initialization error, or an I/O error while reading response bytes
 */
public class StreamingGetConnectionException extends StreamingGetException {

	/**
	 *
	 * @param cause parent cause
	 */
	public StreamingGetConnectionException(Throwable cause) {
		super(cause);
	}
	
}
