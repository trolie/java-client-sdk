package org.trolie.client.request.streaming;

import org.trolie.client.request.streaming.exception.SubscriberException;
import org.trolie.client.request.streaming.exception.SubscriberHandlingException;

/**
 * Base interface for receiver to be invoked when new data is available for a subscribed pollable GET
 */
public interface SubscriptionUpdateReceiver {

	/**
	 * Called when an error occurs establishing, executing or handling the polling request. 
	 * Possible exception types are: 
	 * <p>{@link SubscriberExecutionException} indicating 
	 * connection or request initialization error, or an internal error in the subscriber thread 
	 * <p>{@link SubscriberHandlingException}
	 * which indicates that the request was OK at the HTTP level but failed in content processing.
	 * 
	 * @param t
	 */
	void error(SubscriberException t);
	
	/**
	 * Called after subscription is initialized, allowing receiver to unsubscribe on errors that are deemed fatal
	 * 
	 * @param subscription
	 */
	void setSubscription(RequestSubscription subscription);

}
