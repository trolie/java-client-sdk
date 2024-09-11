package org.trolie.client.request.streaming;

import org.trolie.client.TrolieException;

public abstract class SubscriberRequestException extends TrolieException {

	public SubscriberRequestException(Throwable cause) {
		super(cause);
	}

	public SubscriberRequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public SubscriberRequestException(String message) {
		super(message);
	}
	
}
