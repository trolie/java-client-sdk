package org.trolie.client;

import lombok.Getter;

/**
 * Exception that indicates a failure on the side of the TROLIE endpoint.
 * Covers any non-functional failures, such as connection-loss, HTTP 500 errors etc.
 */
public class TrolieServerException extends TrolieException {

	@Getter
	int httpCode;
	
	public TrolieServerException(int httpCode, String message, Throwable cause) {
		super(message, cause);
		this.httpCode = httpCode;
	}

	
}
