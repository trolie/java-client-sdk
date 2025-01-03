package org.trolie.client.exception;

import lombok.Getter;

/**
 * Exception that indicates a failure on the side of the TROLIE endpoint.
 * Covers any non-functional failures, such as connection-loss, HTTP 500 errors etc.
 */
@Getter
public class TrolieServerException extends TrolieException {

	private final int httpCode;

	/**
	 *
	 * @param httpCode HTTP code from the server
	 * @param message message text, if applicable
	 * @param cause parent, if applicable
	 */
	public TrolieServerException(int httpCode, String message, Throwable cause) {
		super(message, cause);
		this.httpCode = httpCode;
	}

	
}
