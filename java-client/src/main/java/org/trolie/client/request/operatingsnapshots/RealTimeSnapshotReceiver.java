package org.trolie.client.request.operatingsnapshots;

import org.trolie.client.model.operatingsnapshots.RealTimeLimit;
import org.trolie.client.model.operatingsnapshots.RealTimeSnapshotHeader;
import org.trolie.client.request.streaming.StreamingResponseReceiver;

/**
 * Streaming receiver for real-time snapshot response data 
 * 
 * The current request handling can be terminated in any of these methods by throwing an exception.
 * 
 * Errors originating from the subscriber thread will be sent to {@link #error(org.trolie.client.request.streaming.RequestSubscriberException)}
 * 
 */
public interface RealTimeSnapshotReceiver extends StreamingResponseReceiver {

	void beginSnapshot();
	
	void header(RealTimeSnapshotHeader header);
	
	void limit(RealTimeLimit limit);
	
	void endSnapshot();
	
}