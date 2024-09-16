package org.trolie.client.request.streaming.exception;

public class SubscriberHandlingException extends SubscriberException {

	public SubscriberHandlingException(String message, Throwable cause) {
		super(cause);
	}

	public SubscriberHandlingException(String message) {
		super(message);
	}

	public SubscriberHandlingException(Throwable cause) {
		super(cause);
	}
	
}
