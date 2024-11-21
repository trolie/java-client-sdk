package org.trolie.client;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.trolie.client.etag.ETagStore;
import org.trolie.client.etag.MemoryETagStore;
import org.trolie.client.impl.TrolieClientImpl;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class TrolieClientBuilder {

	HttpHost host;
	HttpClient httpClient;
	RequestConfig requestConfig;
	int bufferSize = 4096;
	ObjectMapper objectMapper;
	ETagStore eTagStore;
	Map<String, String> httpHeaders = new HashMap<>();

	public TrolieClientBuilder(
			String baseUrl, 
			HttpClient httpClient) {
		super();
		try {
			this.host = HttpHost.create(baseUrl);
		} catch (URISyntaxException e) {
			throw new TrolieException(e);
		}
		this.httpClient = httpClient;
	}
	
	public TrolieClientBuilder requestConfig(RequestConfig config) {
		this.requestConfig = config;
		return this;
	}
	
	public TrolieClientBuilder bufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
		return this;
	}
	
	public TrolieClientBuilder objectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		return this;
	}
	
	public TrolieClientBuilder etagStore(ETagStore eTagStore) {
		this.eTagStore = eTagStore;
		return this;
	}

	public TrolieClientBuilder httpHeaders(Map<String, String> httpHeaders) {
		this.httpHeaders.clear();
		this.httpHeaders.putAll(httpHeaders);
		return this;
	}

    public TrolieClient build() {
    	
    	if (requestConfig == null) {
    		requestConfig = RequestConfig.DEFAULT;
    	}
    	
    	if (objectMapper == null) {
    		objectMapper = new ObjectMapper();
    	}

    	if (eTagStore == null) {
    		eTagStore = new MemoryETagStore();
    	}

		if (httpHeaders == null) {
			httpHeaders = new HashMap<>();
		}

    	return new TrolieClientImpl(httpClient, host, requestConfig, bufferSize, objectMapper, eTagStore, httpHeaders);
    }
}
