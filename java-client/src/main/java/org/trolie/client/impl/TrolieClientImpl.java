package org.trolie.client.impl;

import java.util.concurrent.ThreadPoolExecutor;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.trolie.client.TrolieClient;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotStreamingReceiver;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscription;
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
	public ForecastSnapshotSubscription subscribeToInUseLimitForecastUpdates(ForecastSnapshotStreamingReceiver receiver, int pollingRateMillis) {
		
		ForecastSnapshotSubscription subscription = new ForecastSnapshotSubscription(
				httpClient, 
				host, 
				requestConfig, 
				bufferSize, 
				threadPoolExecutor, 
				objectMapper, 
				pollingRateMillis, 
				receiver);
		
		subscription.subscribe();
		return subscription;
		
	}

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
