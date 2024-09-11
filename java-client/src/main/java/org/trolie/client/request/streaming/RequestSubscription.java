package org.trolie.client.request.streaming;

import java.util.concurrent.Future;

/**
 * Handle for an active request subscription that is passed to the subscription receiver, allowing it control over the subscription state
 */
public interface RequestSubscription {

	void subscribe();
	
	Future<Void> unsubscribe();
	
}
