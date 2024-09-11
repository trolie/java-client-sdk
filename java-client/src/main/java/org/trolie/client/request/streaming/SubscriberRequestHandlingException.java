package org.trolie.client.request.streaming;

public class SubscriberRequestHandlingException extends SubscriberRequestException {

	public SubscriberRequestHandlingException(String message, Throwable cause) {
		super(cause);
	}

	public SubscriberRequestHandlingException(String message) {
		super(message);
	}

	public SubscriberRequestHandlingException(Throwable cause) {
		super(cause);
	}
	
}
