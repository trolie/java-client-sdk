package energy.trolie.client.exception;

import lombok.Getter;

/**
 * Exception indicating a server response with an abnormal HTTP status code
 */
@Getter
public class StreamingGetResponseException extends StreamingGetException {

	/**
	 * HTTP status code
	 */
	private final int httpCode;

	/**
	 * Creates with an invalid HTTP code
	 * @param cause parent
	 */
	public StreamingGetResponseException(Throwable cause) {

		super(cause);
		httpCode = -1;
	}

	/**
	 *
	 * @param message any captured message
	 * @param code received HTTP response code
	 */
	public StreamingGetResponseException(String message, int code) {
		super(message);
		this.httpCode = code;
	}
}
