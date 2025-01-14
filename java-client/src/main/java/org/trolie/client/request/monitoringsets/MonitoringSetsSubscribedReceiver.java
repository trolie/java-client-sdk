package org.trolie.client.request.monitoringsets;

import org.trolie.client.exception.StreamingGetException;
import org.trolie.client.StreamingSubscribedResponseReceiver;

/**
 * Streaming receiver for getting Monitoring Set.
 * The current request handling can be terminated in any of these methods by throwing an exception.
 * Errors originating from the subscriber thread will be sent to {@link #error(StreamingGetException)}
 */
public interface MonitoringSetsSubscribedReceiver extends StreamingSubscribedResponseReceiver, MonitoringSetsReceiver {

}
	
	
	
