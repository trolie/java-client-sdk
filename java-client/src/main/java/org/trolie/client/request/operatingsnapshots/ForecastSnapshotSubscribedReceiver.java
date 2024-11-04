package org.trolie.client.request.operatingsnapshots;

import org.trolie.client.request.streaming.StreamingSubscribedResponseReceiver;
import org.trolie.client.request.streaming.exception.StreamingGetException;

/**
 * Streaming receiver for updated forecast snapshot data and errors from subscriber.
 * The current request handling can be terminated in any of these methods by throwing an exception.
 * Errors originating from the subscriber thread will be sent to {@link #error(StreamingGetException)}
 */
public interface ForecastSnapshotSubscribedReceiver extends StreamingSubscribedResponseReceiver, ForecastSnapshotReceiver {

}
	
	
	
