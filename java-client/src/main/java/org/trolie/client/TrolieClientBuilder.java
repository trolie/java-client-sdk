package org.trolie.client;


import org.trolie.client.impl.TrolieClientImpl;

import reactor.netty.http.client.HttpClient;

public class TrolieClientBuilder {

	HttpClient httpClient;
	String baseUrl;

	public TrolieClientBuilder(String baseUrl) {
		super();
		this.baseUrl = baseUrl;
	}

	public TrolieClientBuilder withHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
		return this;
	}
	
    public TrolieClient build() {
        
    	if (httpClient == null) {
   			httpClient = HttpClient.create();
    	}
    	
    	httpClient.baseUrl(baseUrl);
    	httpClient.compress(true);
    	return new TrolieClientImpl(httpClient);
    }
}
