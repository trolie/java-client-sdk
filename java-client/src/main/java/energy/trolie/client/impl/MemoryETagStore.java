package energy.trolie.client.impl;

import energy.trolie.client.ETagStore;

import java.util.HashMap;

/**
 * Default ETag store that exists only in memory
 */
public class MemoryETagStore implements ETagStore {

	HashMap<String,String> eTagsByPath = new HashMap<>();
	
	@Override
	public String getETag(String endpointPath) {
		return eTagsByPath.get(endpointPath);
	}

	@Override
	public void putETag(String endpointPath, String eTag) {
		eTagsByPath.put(endpointPath, eTag);
	}
	
}
