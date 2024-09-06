package org.trolie.client;

/**
 * Common base for any exceptions thrown by {@link TrolieClient} to API
 * users.
 */
public class TrolieException extends RuntimeException {

	public TrolieException(String message, Throwable cause) {
		super(message, cause);
	}

	public TrolieException(String message) {
		super(message);
	}

	public TrolieException(Throwable cause) {
		super(cause);
	}
	
	
}
