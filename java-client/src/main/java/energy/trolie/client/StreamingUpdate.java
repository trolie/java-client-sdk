package energy.trolie.client;

/**
 * Base type for streaming updates.  These will have various methods for adding pieces, or
 * "chunks" to the update.  See specific implementation methods for details.  All streaming updates
 * however represent closeable resources, and have an explicit step to complete the payload that returns
 * a status object.
 * @param <T> type of the request status object.
 */
public interface StreamingUpdate<T> extends AutoCloseable {


    /**
     * Complete the update
     * @return returned status
     */
    T complete();
}
