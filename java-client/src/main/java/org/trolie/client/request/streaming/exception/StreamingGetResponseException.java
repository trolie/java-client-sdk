package org.trolie.client.request.streaming.exception;

import lombok.Getter;

public class StreamingGetResponseException extends StreamingGetException {

	@Getter
	int httpCode = -1;
	
	public StreamingGetResponseException(Throwable cause) {
		super(cause);
	}
	
	public StreamingGetResponseException(String message, int code) {
		super(message);
		this.httpCode = code;
	}
}
