package org.trolie.client.request.streaming;

import lombok.Getter;

public class SubscriberRequestExecutionException extends SubscriberRequestException {

	@Getter
	int httpCode = -1;
	
	public SubscriberRequestExecutionException(Throwable cause) {
		super(cause);
	}
	
	public SubscriberRequestExecutionException(String message, int code) {
		super(message);
		this.httpCode = code;
	}
}
