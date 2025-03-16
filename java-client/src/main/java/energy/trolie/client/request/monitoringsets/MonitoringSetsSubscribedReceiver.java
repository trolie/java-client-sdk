package energy.trolie.client.request.monitoringsets;

import energy.trolie.client.exception.StreamingGetException;
import energy.trolie.client.StreamingSubscribedResponseReceiver;

/**
 * Streaming receiver for getting Monitoring Set.
 * The current request handling can be terminated in any of these methods by throwing an exception.
 * Errors originating from the subscriber thread will be sent to {@link #error(StreamingGetException)}
 */
public interface MonitoringSetsSubscribedReceiver extends StreamingSubscribedResponseReceiver, MonitoringSetsReceiver {

}
	
	
	
