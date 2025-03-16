package energy.trolie.client.exception;

/**
 * Wrapper for unexpected errors on data subscription.  Wraps the underlying
 * unexpected error.
 */
public class SubscriberInternalException extends StreamingGetException {

	/**
	 *
	 * @param cause root cause
	 */
	public SubscriberInternalException(Throwable cause) {
		super(cause);
	}
	
}
