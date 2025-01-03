package org.trolie.client;

/**
 * Base interface for receiver to be invoked when new data is available for a subscribed pollable GET
 */
public interface StreamingSubscribedResponseReceiver extends StreamingResponseReceiver {

	/**
	 * Called after subscription is initialized, allowing receiver to unsubscribe on errors that are deemed fatal
	 * 
	 * @param subscription subscription to set
	 */
	default void setSubscription(RequestSubscription subscription) {
	}

}
