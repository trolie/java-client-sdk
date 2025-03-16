package energy.trolie.client.request.operatingsnapshots;

import energy.trolie.client.exception.StreamingGetException;
import energy.trolie.client.StreamingSubscribedResponseReceiver;

/**
 * Streaming receiver for updated real-time snapshot data and errors from subscriber.
 * The current request handling can be terminated in any of these methods by throwing an exception.
 * Errors originating from the subscriber thread will be sent to {@link #error(StreamingGetException)}
 */
public interface RealTimeSnapshotSubscribedReceiver extends StreamingSubscribedResponseReceiver, RealTimeSnapshotReceiver {


}