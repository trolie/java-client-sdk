package org.trolie.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trolie.client.TrolieClient;
import org.trolie.client.ETagStore;
import org.trolie.client.impl.request.RequestSubscriptionInternal;
import org.trolie.client.impl.request.monitoringsets.DefaultMonitoringSetRequest;
import org.trolie.client.impl.request.monitoringsets.DefaultMonitoringSetSubscribedRequest;
import org.trolie.client.request.monitoringsets.MonitoringSetsReceiver;
import org.trolie.client.impl.request.monitoringsets.MonitoringSetsRequest;
import org.trolie.client.request.monitoringsets.MonitoringSetsSubscribedReceiver;
import org.trolie.client.impl.request.monitoringsets.MonitoringSetsSubscribedRequest;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotReceiver;
import org.trolie.client.impl.request.operatingsnapshots.ForecastSnapshotRequest;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscribedReceiver;
import org.trolie.client.impl.request.operatingsnapshots.ForecastSnapshotSubscribedRequest;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotReceiver;
import org.trolie.client.impl.request.operatingsnapshots.RealTimeSnapshotRequest;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotSubscribedReceiver;
import org.trolie.client.impl.request.operatingsnapshots.RealTimeSnapshotSubscribedRequest;
import org.trolie.client.impl.request.operatingsnapshots.RegionalForecastSnapshotRequest;
import org.trolie.client.impl.request.operatingsnapshots.RegionalForecastSubscribedSnapshotRequest;
import org.trolie.client.impl.request.operatingsnapshots.RegionalRealTimeSnapshotRequest;
import org.trolie.client.impl.request.operatingsnapshots.RegionalRealTimeSnapshotSubscribedRequest;
import org.trolie.client.request.ratingproposals.ForecastRatingProposalUpdate;
import org.trolie.client.request.ratingproposals.RealTimeRatingProposalUpdate;
import org.trolie.client.RequestSubscription;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;


public class TrolieClientImpl implements TrolieClient {

	private static final Logger logger = LoggerFactory.getLogger(TrolieClientImpl.class);

	CloseableHttpClient httpClient;
	HttpHost host;
	RequestConfig requestConfig;
	int bufferSize;
	ObjectMapper objectMapper;
	ETagStore eTagStore;
	Map<String, String> httpHeaders;

	public TrolieClientImpl(CloseableHttpClient httpClient, HttpHost host, RequestConfig requestConfig, int bufferSize,
							ObjectMapper objectMapper, ETagStore eTagStore, Map<String, String> httpHeaders) {
		super();
		this.httpClient = httpClient;
		this.host = host;
		this.requestConfig = requestConfig;
		this.bufferSize = bufferSize;
		this.objectMapper = objectMapper;
		this.eTagStore = eTagStore;
		this.httpHeaders = httpHeaders;
	}

	final Set<RequestSubscriptionInternal> activeSubscriptions = new HashSet<>();

	protected void addSubscription(RequestSubscriptionInternal subscription) {
		synchronized (activeSubscriptions) {
			activeSubscriptions.add(subscription);
		}
		subscription.start();
	}

	@Override
	public void getInUseLimitForecasts(ForecastSnapshotReceiver receiver) {
		getInUseLimitForecasts(receiver, null, null, null, null);
	}
	
	@Override
	public void getInUseLimitForecasts(ForecastSnapshotReceiver receiver, String monitoringSet) {
		getInUseLimitForecasts(receiver, monitoringSet, null,null,null);
	}
	
	@Override
	public void getInUseLimitForecasts(ForecastSnapshotReceiver receiver, Instant periodStart, Instant periodEnd) {
		getInUseLimitForecasts(receiver, null, null, periodStart, periodEnd);
	}
	
	@Override
	public void getInUseLimitForecasts(
			ForecastSnapshotReceiver receiver,
			String monitoringSet,
			String resourceId,
			Instant periodStart,
			Instant periodEnd) {
		
		new ForecastSnapshotRequest(
				httpClient, 
				host, 
				requestConfig, 
				bufferSize, 
				objectMapper,
				httpHeaders,
				receiver,
				monitoringSet,
				resourceId,
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
				objectMapper,
				httpHeaders,
				pollingRateMillis,
				receiver,
				eTagStore,
				monitoringSet);
		
		addSubscription(subscription);
		return subscription;
		
	}

	@Override
	public void getRegionalLimitsForecast(ForecastSnapshotReceiver receiver) {
		getRegionalLimitsForecast(receiver, null, null, null, null);
	}

	@Override
	public void getRegionalLimitsForecast(ForecastSnapshotReceiver receiver, String monitoringSet) {
		getRegionalLimitsForecast(receiver, monitoringSet, null,null,null);
	}

	@Override
	public void getRegionalLimitsForecast(ForecastSnapshotReceiver receiver, Instant periodStart, Instant periodEnd) {
		getRegionalLimitsForecast(receiver, null, null, periodStart, periodEnd);
	}

	@Override
	public void getRegionalLimitsForecast(
			ForecastSnapshotReceiver receiver,
			String monitoringSet,
			String resourceId,
			Instant periodStart,
			Instant periodEnd) {

		new RegionalForecastSnapshotRequest(
				httpClient,
				host,
				requestConfig,
				bufferSize,
				objectMapper,
				httpHeaders,
				receiver,
				monitoringSet,
				resourceId,
				periodStart,
				periodEnd).executeRequest();

	}

	@Override
	public RegionalForecastSubscribedSnapshotRequest subscribeToRegionalLimitsForecast(
			ForecastSnapshotSubscribedReceiver receiver,
			int pollingRateMillis) {

		return subscribeToRegionalLimitsForecast(receiver, null, pollingRateMillis);
	}

	@Override
	public RegionalForecastSubscribedSnapshotRequest subscribeToRegionalLimitsForecast(
			ForecastSnapshotSubscribedReceiver receiver,
			String monitoringSet,
			int pollingRateMillis) {

		RegionalForecastSubscribedSnapshotRequest subscription = new RegionalForecastSubscribedSnapshotRequest(
				httpClient,
				host,
				requestConfig,
				bufferSize,
				objectMapper,
				httpHeaders,
				pollingRateMillis,
				receiver,
				eTagStore,
				monitoringSet);

		addSubscription(subscription);
		return subscription;
	}

	@Override
	public ForecastRatingProposalUpdate createForecastRatingProposalStreamingUpdate() {
		return new ForecastRatingProposalUpdate(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders);
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
			String monitoringSet, String resourceId, int pollingRateMillis) {

		RealTimeSnapshotSubscribedRequest subscription = new RealTimeSnapshotSubscribedRequest(
				httpClient, 
				host, 
				requestConfig, 
				bufferSize, 
				objectMapper,
				httpHeaders,
				pollingRateMillis,
				receiver,
				eTagStore,
				monitoringSet,
				resourceId);
		
		addSubscription(subscription);
		return subscription;
	}

	@Override
	public RealTimeRatingProposalUpdate createRealTimeRatingProposalStreamingUpdate() {
		return new RealTimeRatingProposalUpdate(httpClient, host, requestConfig, bufferSize,
				objectMapper, httpHeaders);
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
	public void getInUseLimits(RealTimeSnapshotReceiver receiver, String monitoringSet, String resourceId) {
		
		new RealTimeSnapshotRequest(
				httpClient, 
				host, 
				requestConfig, 
				bufferSize,
				objectMapper,
				httpHeaders,
				receiver,
				monitoringSet,
				resourceId).executeRequest();
		
	}

	@Override
	public void getRegionalRealTimeLimits(RealTimeSnapshotReceiver receiver) {
		getRegionalRealTimeLimits(receiver, null, null);
	}

	@Override
	public void getRegionalRealTimeLimits(RealTimeSnapshotReceiver receiver, String monitoringSet) {
		getRegionalRealTimeLimits(receiver, monitoringSet, null);
	}

	@Override
	public void getRegionalRealTimeLimits(RealTimeSnapshotReceiver receiver, String monitoringSet, String resourceId) {

		new RegionalRealTimeSnapshotRequest(
				httpClient,
				host,
				requestConfig,
				bufferSize,
				objectMapper,
				httpHeaders,
				receiver,
				monitoringSet,
				resourceId).executeRequest();
	}

	@Override
	public RegionalRealTimeSnapshotSubscribedRequest subscribeToRegionalRealTimeLimits(
			RealTimeSnapshotSubscribedReceiver receiver,
			int pollingRateMillis) {
		return subscribeToRegionalRealTimeLimits(receiver, null, pollingRateMillis);
	}

	@Override
	public RegionalRealTimeSnapshotSubscribedRequest subscribeToRegionalRealTimeLimits(
			RealTimeSnapshotSubscribedReceiver receiver,
			String monitoringSet,
			int pollingRateMillis) {

		RegionalRealTimeSnapshotSubscribedRequest subscription = new RegionalRealTimeSnapshotSubscribedRequest(
				httpClient,
				host,
				requestConfig,
				bufferSize,
				objectMapper,
				httpHeaders,
				pollingRateMillis,
				receiver,
				eTagStore,
				monitoringSet);

		addSubscription(subscription);
		return subscription;
	}

	@Override
	public void getMonitoringSet(MonitoringSetsReceiver receiver, String monitoringSet) {
		new MonitoringSetsRequest(
				httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders,
				receiver, monitoringSet).executeRequest();

	}

	@Override
	public MonitoringSetsSubscribedRequest subscribeToMonitoringSetUpdates(MonitoringSetsSubscribedReceiver receiver,
																		   String monitoringSet,
																		   int pollingRateMillis) {
		MonitoringSetsSubscribedRequest subscription = new MonitoringSetsSubscribedRequest(
				httpClient, host, requestConfig, pollingRateMillis, objectMapper, httpHeaders,
				pollingRateMillis, receiver, eTagStore, monitoringSet);
		addSubscription(subscription);
		return subscription;
	}

	@Override
	public void getDefaultMonitoringSet(MonitoringSetsReceiver receiver) {
		new DefaultMonitoringSetRequest(
				httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders, receiver)
				.executeRequest();
	}

	@Override
	public DefaultMonitoringSetSubscribedRequest subscribeToDefaultMonitoringSetUpdates(
			MonitoringSetsSubscribedReceiver receiver, int pollingRateMillis) {
		var subscription = new DefaultMonitoringSetSubscribedRequest(
				httpClient, host, requestConfig, pollingRateMillis, objectMapper, httpHeaders,
				pollingRateMillis, receiver, eTagStore);
		addSubscription(subscription);
		return subscription;
	}

	@Override
	public void close() throws IOException {
		logger.info("Closing all subscriptions");
		unsubscribeAll();

		logger.debug("Closing HTTP Client");
		httpClient.close();

	}

	@Override
	public void unsubscribe(RequestSubscription subscription) {
		try {
			((RequestSubscriptionInternal)subscription).stop().get();
		} catch (ExecutionException e) {
			logger.error("Error in request subscription {}", subscription, e);
		} catch (InterruptedException e) {
			logger.info("Request subscription interrupted {}", subscription);
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void unsubscribeAll() {
		activeSubscriptions.forEach(this::unsubscribe);
	}

}
