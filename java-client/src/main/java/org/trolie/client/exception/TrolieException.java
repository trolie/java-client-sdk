package org.trolie.client.exception;

import org.trolie.client.TrolieClient;

/**
 * Common base for any exceptions thrown by {@link TrolieClient} to API
 * users.
 */
public class TrolieException extends RuntimeException {

	/**
	 *
	 * @param message message
	 * @param cause parent
	 */
	public TrolieException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 *
	 * @param message message
	 */
	public TrolieException(String message) {
		super(message);
	}

	/**
	 *
	 * @param cause parent
	 */
	public TrolieException(Throwable cause) {
		super(cause);
	}
	
	
}
