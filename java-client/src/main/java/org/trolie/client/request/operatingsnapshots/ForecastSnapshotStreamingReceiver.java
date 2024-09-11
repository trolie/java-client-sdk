package org.trolie.client.request.operatingsnapshots;

import org.trolie.client.model.operatingsnapshots.ForecastPeriodSnapshot;
import org.trolie.client.model.operatingsnapshots.ForecastSnapshotHeader;
import org.trolie.client.request.streaming.SubscriptionUpdateReceiver;

/**
 * Streaming receiver for updated forecast snapshot data and errors from subscriber. 
 * 
 * The current request handling can be terminated in any of these methods by throwing an exception.
 * 
 * Errors originating from the subscriber thread will be sent to {@link #error(org.trolie.client.request.streaming.RequestSubscriberException)}
 * 
 */
public interface ForecastSnapshotStreamingReceiver extends SubscriptionUpdateReceiver {

	void beginSnapshot();
	
	void header(ForecastSnapshotHeader header);
	
	void beginResource(String resourceId);
	
	void period(ForecastPeriodSnapshot period);
	
	void endResource();
	
	void endSnapshot();
	
}
	
	
	
