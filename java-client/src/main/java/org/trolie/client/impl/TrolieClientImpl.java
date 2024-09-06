package org.trolie.client.impl;

import java.util.concurrent.ThreadPoolExecutor;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.trolie.client.TrolieClient;
import org.trolie.client.request.ratingproposals.ForecastRatingProposalStreamingUpdate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TrolieClientImpl implements TrolieClient {

	HttpClient httpClient;
	HttpHost host;
	RequestConfig requestConfig;
	int bufferSize;
	ThreadPoolExecutor threadPoolExecutor;
	ObjectMapper objectMapper;
	
	@Override
	public void getInUseLimitForecasts(String monitoringSet) {}

	@Override
	public void subscribeToInUseLimitForecastUpdates() {}

	@Override
	public ForecastRatingProposalStreamingUpdate createForecastRatingProposalStreamingUpdate() {
		return new ForecastRatingProposalStreamingUpdate(httpClient, host, requestConfig, threadPoolExecutor, bufferSize, objectMapper);
	}

	@Override
	public void getInUseLimits() {}

	@Override
	public void subscribeToInUseLimits() {}

	@Override
	public void updateRealTimeProposal() {}

}
