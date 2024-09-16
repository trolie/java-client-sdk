package org.trolie.client.request.streaming.exception;

import lombok.Getter;

public class SubscriberResponseException extends SubscriberException {

	@Getter
	int httpCode = -1;
	
	public SubscriberResponseException(Throwable cause) {
		super(cause);
	}
	
	public SubscriberResponseException(String message, int code) {
		super(message);
		this.httpCode = code;
	}
}
