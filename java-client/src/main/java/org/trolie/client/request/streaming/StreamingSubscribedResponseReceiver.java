package org.trolie.client.request.streaming;

/**
 * Base interface for receiver to be invoked when new data is available for a subscribed pollable GET
 */
public interface StreamingSubscribedResponseReceiver extends StreamingResponseReceiver {

	/**
	 * Called after subscription is initialized, allowing receiver to unsubscribe on errors that are deemed fatal
	 * 
	 * @param subscription
	 */
	void setSubscription(RequestSubscription subscription);

}
