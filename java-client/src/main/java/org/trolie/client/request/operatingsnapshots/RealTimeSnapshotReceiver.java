package org.trolie.client.request.operatingsnapshots;

import org.trolie.client.model.operatingsnapshots.RealTimeLimit;
import org.trolie.client.model.operatingsnapshots.RealTimeSnapshotHeader;
import org.trolie.client.request.streaming.StreamingResponseReceiver;
import org.trolie.client.request.streaming.exception.StreamingGetException;

/**
 * Streaming receiver for real-time snapshot response data
 * The current request handling can be terminated in any of these methods by throwing an exception.
 * Errors originating from the subscriber thread will be sent to {@link #error(StreamingGetException)}
 */
public interface RealTimeSnapshotReceiver extends StreamingResponseReceiver {

	void beginSnapshot();

	void header(RealTimeSnapshotHeader header);

	void limit(RealTimeLimit limit);

	void endSnapshot();

}