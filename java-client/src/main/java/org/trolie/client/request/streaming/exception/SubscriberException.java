package org.trolie.client.request.streaming.exception;

import org.trolie.client.TrolieException;

public abstract class SubscriberException extends TrolieException {

	public SubscriberException(Throwable cause) {
		super(cause);
	}

	public SubscriberException(String message, Throwable cause) {
		super(message, cause);
	}

	public SubscriberException(String message) {
		super(message);
	}
	
}
