package org.trolie.client.etag;

/**
 * Pluggable store for request ETag values 
 */
public interface ETagStore {

	String getETag(String endpointPath);
	
	void putETag(String endpointPath, String eTag);
	
}
