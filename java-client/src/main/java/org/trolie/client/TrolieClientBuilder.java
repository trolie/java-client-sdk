package org.trolie.client;


import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.trolie.client.impl.TrolieClientImpl;


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
   			httpClient = HttpClients.createDefault();
    	}

		// TODO: Add RequestConfig, HttpHost, and SSL config. Maybe don't make user provide HttpClient?

    	return new TrolieClientImpl(httpClient, baseUrl);
    }
}
