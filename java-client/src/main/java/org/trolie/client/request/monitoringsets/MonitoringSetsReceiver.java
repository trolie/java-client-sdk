package org.trolie.client.request.monitoringsets;

import org.trolie.client.model.monitoringsets.MonitoringSet;
import org.trolie.client.request.streaming.StreamingResponseReceiver;

/**
 * Streaming receiver for updated forecast snapshot data and errors from subscriber. 
 * 
 * The current request handling can be terminated in any of these methods by throwing an exception.
 * 
 * Errors originating from the subscriber thread will be sent to {@link #error(org.trolie.client.request.streaming.RequestSubscriberException)}
 * 
 */
public interface MonitoringSetsReceiver extends StreamingResponseReceiver {

	void begin();
	
	void header(MonitoringSet monitoringSet);
	
	void end();
	
}
	
	
	
