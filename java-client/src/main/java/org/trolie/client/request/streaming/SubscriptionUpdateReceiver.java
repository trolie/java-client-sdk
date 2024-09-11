package org.trolie.client.request.streaming;

/**
 * Base interface for receiver to be invoked when new data is available for a subscribed pollable GET
 */
public interface SubscriptionUpdateReceiver {

	/**
	 * Called when an error occurs establishing, executing or handling the polling request. 
	 * 
	 * @param t
	 */
	void error(SubscriberRequestException t);
	
	/**
	 * Called after subscription is initialized, allowing receiver to unsubscribe on errors that are deemed fatal
	 * 
	 * @param subscription
	 */
	void setSubscription(RequestSubscription subscription);
}
