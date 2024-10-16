package org.trolie.client.impl;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.trolie.client.TrolieClient;
import org.trolie.client.etag.ETagStore;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotReceiver;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotRequest;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscribedReceiver;
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
	Map<String, String> httpHeader;
	boolean enableCompression;
	
	@Override
	public void getInUseLimitForecasts(ForecastSnapshotReceiver receiver) {
		getInUseLimitForecasts(receiver,null,null,null);
	}
	
	@Override
	public void getInUseLimitForecasts(ForecastSnapshotReceiver receiver, String monitoringSet) {
		getInUseLimitForecasts(receiver,monitoringSet,null,null);
	}
	
	@Override
	public void getInUseLimitForecasts(ForecastSnapshotReceiver receiver, Instant periodStart, Instant periodEnd) {
		getInUseLimitForecasts(receiver, null, periodStart, periodEnd);
	}
	
	@Override
	public void getInUseLimitForecasts(
			ForecastSnapshotReceiver receiver,
			String monitoringSet,
			Instant periodStart,
			Instant periodEnd) {
		
		new ForecastSnapshotRequest(
				httpClient, 
				host, 
				requestConfig, 
				bufferSize, 
				threadPoolExecutor, 
				objectMapper, 
				receiver, 
				monitoringSet,
				periodStart,
				periodEnd).executeRequest();
		
	}

	@Override
	public ForecastSnapshotSubscribedRequest subscribeToInUseLimitForecastUpdates(
			ForecastSnapshotSubscribedReceiver receiver, int pollingRateMillis) {
		return subscribeToInUseLimitForecastUpdates(receiver, null, pollingRateMillis);
	}
	
	@Override
	public ForecastSnapshotSubscribedRequest subscribeToInUseLimitForecastUpdates(
			ForecastSnapshotSubscribedReceiver receiver,
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
		return new ForecastRatingProposalUpdate(httpClient, host, requestConfig, threadPoolExecutor, bufferSize, objectMapper, httpHeader, enableCompression);
	}

	@Override
	public RealTimeSnapshotSubscribedRequest subscribeToInUseLimits(RealTimeSnapshotSubscribedReceiver receiver,
			int pollingRateMillis) {
		return subscribeToInUseLimits(receiver, null, null, pollingRateMillis);
	}

	@Override
	public RealTimeSnapshotSubscribedRequest subscribeToInUseLimits(RealTimeSnapshotSubscribedReceiver receiver,
			String monitoringSet, int pollingRateMillis) {
		return subscribeToInUseLimits(receiver, monitoringSet, null, pollingRateMillis);
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
		return new RealTimeRatingProposalUpdate(httpClient, host, requestConfig, threadPoolExecutor, bufferSize, objectMapper, httpHeader, enableCompression);
	}

	@Override
	public void getInUseLimits(RealTimeSnapshotReceiver receiver) {
		getInUseLimits(receiver, null, null);
	}
	
	@Override
	public void getInUseLimits(RealTimeSnapshotReceiver receiver, String monitoringSet) {
		getInUseLimits(receiver, monitoringSet, null);
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
