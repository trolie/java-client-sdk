package org.trolie.client.impl;

import java.util.concurrent.ThreadPoolExecutor;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.trolie.client.TrolieClient;
import org.trolie.client.etag.ETagStore;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotReceiver;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscribedRequest;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotReceiver;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotRequest;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotSubscribedReceiver;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotSubscribedRequest;
import org.trolie.client.request.ratingproposals.ForecastRatingProposalUpdate;
import org.trolie.client.request.ratingproposals.RealTimeRatingProposalUpdate;

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
	ETagStore eTagStore;
	
	@Override
	public void getInUseLimitForecasts(String monitoringSet) {}

	@Override
	public ForecastSnapshotSubscribedRequest subscribeToInUseLimitForecastUpdates(
			ForecastSnapshotReceiver receiver,
			String monitoringSet,
			int pollingRateMillis) {
		
		ForecastSnapshotSubscribedRequest subscription = new ForecastSnapshotSubscribedRequest(
				httpClient, 
				host, 
				requestConfig, 
				bufferSize, 
				threadPoolExecutor, 
				objectMapper, 
				pollingRateMillis, 
				receiver,
				eTagStore,
				monitoringSet);
		
		subscription.subscribe();
		return subscription;
		
	}

	@Override
	public ForecastRatingProposalUpdate createForecastRatingProposalStreamingUpdate() {
		return new ForecastRatingProposalUpdate(httpClient, host, requestConfig, threadPoolExecutor, bufferSize, objectMapper);
	}

	@Override
	public RealTimeSnapshotSubscribedRequest subscribeToInUseLimits(RealTimeSnapshotSubscribedReceiver receiver,
			String monitoringSet, String transmissionFacility, int pollingRateMillis) {

		RealTimeSnapshotSubscribedRequest subscription = new RealTimeSnapshotSubscribedRequest(
				httpClient, 
				host, 
				requestConfig, 
				bufferSize, 
				threadPoolExecutor, 
				objectMapper, 
				pollingRateMillis, 
				receiver,
				eTagStore,
				monitoringSet,
				transmissionFacility);
		
		subscription.subscribe();
		return subscription;
	}

	@Override
	public RealTimeRatingProposalUpdate createRealTimeRatingProposalStreamingUpdate() {
		return new RealTimeRatingProposalUpdate(httpClient, host, requestConfig, threadPoolExecutor, bufferSize, objectMapper);
	}
	
	@Override
	public void getInUseLimits(RealTimeSnapshotReceiver receiver, String monitoringSet, String transmissionFacility) {
		
		new RealTimeSnapshotRequest(
				httpClient, 
				host, 
				requestConfig, 
				bufferSize, 
				threadPoolExecutor, 
				objectMapper, 
				receiver, 
				monitoringSet, 
				transmissionFacility).executeRequest();
		
	}


}
