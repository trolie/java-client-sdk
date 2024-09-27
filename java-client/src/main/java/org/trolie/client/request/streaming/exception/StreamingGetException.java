package org.trolie.client.request.streaming.exception;

import org.trolie.client.TrolieException;

public abstract class StreamingGetException extends TrolieException {

	public StreamingGetException(Throwable cause) {
		super(cause);
	}

	public StreamingGetException(String message, Throwable cause) {
		super(message, cause);
	}

	public StreamingGetException(String message) {
		super(message);
	}
	
}
