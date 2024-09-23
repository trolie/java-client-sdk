package org.trolie.client.request.streaming.exception;

public class StreamingGetHandlingException extends StreamingGetException {

	public StreamingGetHandlingException(String message, Throwable cause) {
		super(cause);
	}

	public StreamingGetHandlingException(String message) {
		super(message);
	}

	public StreamingGetHandlingException(Throwable cause) {
		super(cause);
	}
	
}
