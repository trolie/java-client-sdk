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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TrolieClientBuilder {

	HttpHost host;
	HttpClient httpClient;
	ThreadPoolExecutor threadPoolExecutor;
	RequestConfig requestConfig;
	int bufferSize = 4096;
	ObjectMapper objectMapper;
	ETagStore eTagStore;
	Map<String, String> httpHeader = new HashMap<>();

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

	public TrolieClientBuilder threadPoolExecutor(ThreadPoolExecutor executor) {
		this.threadPoolExecutor = executor;
		return this;
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

	public TrolieClientBuilder httpHeader(Map<String, String> httpHeader) {
		this.httpHeader.clear();
		this.httpHeader.putAll(httpHeader);
		return this;
	}

    public TrolieClient build() {

    	if (threadPoolExecutor == null) {
    		threadPoolExecutor = new ThreadPoolExecutor(1, 1, 1,
					TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    	}
    	
    	if (requestConfig == null) {
    		requestConfig = RequestConfig.DEFAULT;
    	}
    	
    	if (objectMapper == null) {
    		objectMapper = new ObjectMapper();
    	}

    	if (eTagStore == null) {
    		eTagStore = new MemoryETagStore();
    	}
		if (httpHeader == null) {
			httpHeader = new HashMap<>();
		}

    	return new TrolieClientImpl(httpClient, host, requestConfig, bufferSize, threadPoolExecutor, objectMapper,
				eTagStore, httpHeader);
    }
}
