package org.trolie.client.request.operatingsnapshots;

import org.trolie.client.model.operatingsnapshots.ForecastPeriodSnapshot;
import org.trolie.client.model.operatingsnapshots.ForecastSnapshotHeader;
import org.trolie.client.StreamingResponseReceiver;
import org.trolie.client.exception.StreamingGetException;

/**
 * Streaming receiver for updated forecast snapshot data and errors from subscriber.
 * The current request handling can be terminated in any of these methods by throwing an exception.
 * Errors originating from the subscriber thread will be sent to {@link #error(StreamingGetException)}
 */
public interface ForecastSnapshotReceiver extends StreamingResponseReceiver {

    /**
     * Invoked when a new snapshot is received.  This will be the first method invoked when the snapshot is received.
     */
    void beginSnapshot();

    /**
     * Invoked when the header has been processed.  Will
     * be invoked before processing resources
     * @param header parsed header
     */
    void header(ForecastSnapshotHeader header);

    /**
     * Invoked to indicate that a new resource has been found while parsing.
     * Implementations may assume that all period ratings for this resource will
     * be consumed through invocations to {@link #period(ForecastPeriodSnapshot)}
     * until {@link #endResource()} is invoked.
     * @param resourceId the next resource Id.
     */
    void beginResource(String resourceId);

    /**
     * Invoked as a new rating value set is encountered in the stream
     * @param period rating data for a given period.  The resource ID
     *               is implied from the context, assuming the last call
     *               to {@link #beginResource(String)}.
     */
    void period(ForecastPeriodSnapshot period);

    /**
     * Invoked when the end of a set of ratings for the current resource has been encountered.
     * Users may assume that the last resource set by {@link #beginResource(String)} is no longer valid.
     */
    void endResource();

    /**
     * Invoked when the snapshot has reached its end.
     */
    void endSnapshot();

}
	
	
	
