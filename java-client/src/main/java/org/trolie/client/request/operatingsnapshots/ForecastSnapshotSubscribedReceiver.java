package org.trolie.client.request.operatingsnapshots;

import org.trolie.client.request.streaming.StreamingSubscribedResponseReceiver;

/**
 * Streaming receiver for updated forecast snapshot data and errors from subscriber. 
 * 
 * The current request handling can be terminated in any of these methods by throwing an exception.
 * 
 * Errors originating from the subscriber thread will be sent to {@link #error(org.trolie.client.request.streaming.RequestSubscriberException)}
 * 
 */
public interface ForecastSnapshotSubscribedReceiver extends StreamingSubscribedResponseReceiver,ForecastSnapshotReceiver {

}
	
	
	
