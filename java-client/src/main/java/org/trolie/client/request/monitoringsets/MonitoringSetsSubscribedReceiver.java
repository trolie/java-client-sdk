package org.trolie.client.request.monitoringsets;

import org.trolie.client.request.streaming.StreamingSubscribedResponseReceiver;

/**
 * Streaming receiver for getting Monitoring Set. 
 * 
 * The current request handling can be terminated in any of these methods by throwing an exception.
 * 
 * Errors originating from the subscriber thread will be sent to {@link #error(org.trolie.client.request.streaming.RequestSubscriberException)}
 * 
 */
public interface MonitoringSetsSubscribedReceiver extends StreamingSubscribedResponseReceiver, MonitoringSetsReceiver {

}
	
	
	
