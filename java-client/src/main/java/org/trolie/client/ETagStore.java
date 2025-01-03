package org.trolie.client;

/**
 * <p>Pluggable store for request ETag values.</p>
 * <p>As described in <a href="https://trolie.energy/articles/conditional-GET.html">Conditional GET</a>,
 * the TROLIE specification relies heavily on
 * use of Conditional GETs, using the standard ETag response header (see
 * <a href="https://datatracker.ietf.org/doc/html/rfc7232#section-2.3">standards</a>) to mark
 * the version of data returned.  </p>
 * <p>Implementations of this interface provide key-value storage for these
 * ETags as application state.  By default, the {@link TrolieClientBuilder} will
 * inject an in-memory implementation of this store.  Users may wish to
 * override this interface however to use storage such as a database that better
 * maps to their own application architecture.</p>
 */
public interface ETagStore {

	/**
	 * Get the currently stored eTag for this particular path.
	 * @param endpointPath The full path at which data is requested is effectively
	 *                     the key to look up the data.
	 * @return raw string value if stored, null if non-existent.
	 */
	String getETag(String endpointPath);

	/**
	 * Store a new value at this particular path.
	 * @param endpointPath The full path at which data is requested is effectively
	 *                     the key to look up the data.
	 * @param eTag value of the eTag as returned in the response header from
	 *             the TROLIE server.
	 */
	void putETag(String endpointPath, String eTag);
	
}
