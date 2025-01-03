package org.trolie.client.request.monitoringsets;

import org.trolie.client.model.monitoringsets.MonitoringSet;
import org.trolie.client.exception.StreamingGetException;
import org.trolie.client.StreamingResponseReceiver;

/**
 * Streaming receiver for updated monitoring set data and errors from subscriber.
 * The current request handling can be terminated in any of these methods by throwing an exception.
 * Errors originating from the subscriber thread will be sent to {@link #error(StreamingGetException)}
 */
public interface MonitoringSetsReceiver extends StreamingResponseReceiver {

    /**
     * Called when a monitoring set is received successfully
     * @param monitoringSet received monitoring set
     */
    void monitoringSet(MonitoringSet monitoringSet);

}
	
	
	
