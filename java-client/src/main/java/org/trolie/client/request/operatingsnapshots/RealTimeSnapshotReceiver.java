package org.trolie.client.request.operatingsnapshots;

import org.trolie.client.model.operatingsnapshots.RealTimeLimit;
import org.trolie.client.model.operatingsnapshots.RealTimeSnapshotHeader;
import org.trolie.client.StreamingResponseReceiver;
import org.trolie.client.exception.StreamingGetException;

/**
 * Streaming receiver for real-time snapshot response data
 * The current request handling can be terminated in any of these methods by throwing an exception.
 * Errors originating from the subscriber thread will be sent to {@link #error(StreamingGetException)}
 */
public interface RealTimeSnapshotReceiver extends StreamingResponseReceiver {

	/**
	 * Invoked when a new snapshot is received.  This will be the first method invoked when the snapshot is received.
	 */
	void beginSnapshot();

	/**
	 * Invoked when the header has been processed.  Will
	 * be invoked before processing resources
	 * @param header parsed header
	 */
	void header(RealTimeSnapshotHeader header);

	/**
	 * Invoked with each resource limit set as it is parsed
	 * @param limit the parsed limit set
	 */
	void limit(RealTimeLimit limit);

	/**
	 * Invoked when the snapshot has reached its end.
	 */
	void endSnapshot();

}