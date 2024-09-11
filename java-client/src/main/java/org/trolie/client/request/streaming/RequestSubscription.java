package org.trolie.client.request.streaming;

import java.util.concurrent.Future;

public interface RequestSubscription {

	void subscribe();
	
	Future<Void> unsubscribe();
	
}
