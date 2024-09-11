package org.trolie.client.request.streaming;

import org.trolie.client.request.streaming.exception.SubscriberConnectionException;
import org.trolie.client.request.streaming.exception.SubscriberException;
import org.trolie.client.request.streaming.exception.SubscriberHandlingException;
import org.trolie.client.request.streaming.exception.SubscriberInternalException;
import org.trolie.client.request.streaming.exception.SubscriberResponseException;

/**
 * Base interface for receiver to be invoked when new data is available for a subscribed pollable GET
 */
public interface SubscriptionUpdateReceiver {

	/**
	 * Called when an error occurs establishing, executing or handling the polling request. 
	 * Possible exception types are: 
	 * <p>{@link SubscriberConnectionException} indicating connection or request initialization error, or an I/O error while reading response bytes
	 * <p>{@link SubscriberResponseException} indicating a server response with an abnormal HTTP status code 
	 * <p>{@link SubscriberHandlingException} indicating an issue with interpreting the response bytes
	 * <p>{@link SubscriberInternalException} indicating an internal error in the subscriber thread.
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
