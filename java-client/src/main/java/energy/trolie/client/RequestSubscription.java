package energy.trolie.client;

/**
 * Handle for an active request subscription that is passed to the subscription receiver,
 * allowing it to control the subscription state
 */
public interface RequestSubscription {

	/**
	 *
	 * @return true if the subscription is currently running.
	 */
	boolean isActive();

	/**
	 *
	 * @return true if the subscription is running and has not been stopped
	 */
	boolean isSubscribed();

	/**
	 *
	 * @return true if the subscription is currently healthy, meaning that the last poll
	 * occurred successfully without error.
	 */
	boolean isHealthy();
}
