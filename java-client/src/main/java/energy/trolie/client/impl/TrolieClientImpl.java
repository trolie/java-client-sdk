package energy.trolie.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import energy.trolie.client.ETagStore;
import energy.trolie.client.RequestSubscription;
import energy.trolie.client.TrolieClient;
import energy.trolie.client.TrolieHost;
import energy.trolie.client.impl.request.RequestSubscriptionInternal;
import energy.trolie.client.impl.request.monitoringsets.DefaultMonitoringSetRequest;
import energy.trolie.client.impl.request.monitoringsets.DefaultMonitoringSetSubscribedRequest;
import energy.trolie.client.impl.request.monitoringsets.MonitoringSetsRequest;
import energy.trolie.client.impl.request.monitoringsets.MonitoringSetsSubscribedRequest;
import energy.trolie.client.impl.request.operatingsnapshots.*;
import energy.trolie.client.request.monitoringsets.MonitoringSetsReceiver;
import energy.trolie.client.request.monitoringsets.MonitoringSetsSubscribedReceiver;
import energy.trolie.client.request.operatingsnapshots.*;
import energy.trolie.client.request.ratingproposals.ForecastRatingProposalUpdate;
import energy.trolie.client.request.ratingproposals.RealTimeRatingProposalUpdate;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;


public class TrolieClientImpl implements TrolieClient {

	private static final Logger logger = LoggerFactory.getLogger(TrolieClientImpl.class);

	CloseableHttpClient httpClient;
	TrolieHost host;
	RequestConfig requestConfig;
	int bufferSize;
	ObjectMapper objectMapper;
	ETagStore eTagStore;
	Map<String, String> httpHeaders;
	private final int defaultIntervalMinutes;
	private final int realTimeRatingsPollMs;
	private final int forecastRatingsPollMs;
	private final int monitoringSetPollMs;

	public TrolieClientImpl(CloseableHttpClient httpClient, TrolieHost host, RequestConfig requestConfig, int bufferSize,
							ObjectMapper objectMapper, ETagStore eTagStore, Map<String, String> httpHeaders,
							int defaultIntervalMinutes,
							int realTimeRatingsPollMs,
							int forecastRatingsPollMs,
							int monitoringSetPollMs) {
		super();
		this.httpClient = httpClient;
		this.host = host;
		this.requestConfig = requestConfig;
		this.bufferSize = bufferSize;
		this.objectMapper = objectMapper;
		this.eTagStore = eTagStore;
		this.httpHeaders = httpHeaders;
		this.defaultIntervalMinutes = defaultIntervalMinutes;
		this.realTimeRatingsPollMs = realTimeRatingsPollMs;
		this.forecastRatingsPollMs = forecastRatingsPollMs;
		this.monitoringSetPollMs = monitoringSetPollMs;
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
			ForecastSnapshotSubscribedReceiver receiver) {
		return subscribeToInUseLimitForecastUpdates(receiver, null);
	}

	@Override
	public ForecastSnapshotSubscribedRequest subscribeToInUseLimitForecastUpdates(
			ForecastSnapshotSubscribedReceiver receiver,
			String monitoringSet) {
		
		ForecastSnapshotSubscribedRequest subscription = new ForecastSnapshotSubscribedRequest(
				httpClient, 
				host, 
				requestConfig, 
				bufferSize, 
				objectMapper,
				httpHeaders,
				forecastRatingsPollMs,
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
			ForecastSnapshotSubscribedReceiver receiver) {

		return subscribeToRegionalLimitsForecast(receiver, null);
	}

	@Override
	public RegionalForecastSubscribedSnapshotRequest subscribeToRegionalLimitsForecast(
			ForecastSnapshotSubscribedReceiver receiver,
			String monitoringSet) {

		RegionalForecastSubscribedSnapshotRequest subscription = new RegionalForecastSubscribedSnapshotRequest(
				httpClient,
				host,
				requestConfig,
				bufferSize,
				objectMapper,
				httpHeaders,
				forecastRatingsPollMs,
				receiver,
				eTagStore,
				monitoringSet);

		addSubscription(subscription);
		return subscription;
	}

	@Override
	public ForecastRatingProposalUpdate createForecastRatingProposalStreamingUpdate() {
		return new ForecastRatingProposalUpdate(httpClient, host, requestConfig, bufferSize,
				objectMapper, httpHeaders, defaultIntervalMinutes);
	}

	@Override
	public RealTimeSnapshotSubscribedRequest subscribeToInUseLimits(RealTimeSnapshotSubscribedReceiver receiver) {
		return subscribeToInUseLimits(receiver, null, null);
	}

	@Override
	public RealTimeSnapshotSubscribedRequest subscribeToInUseLimits(RealTimeSnapshotSubscribedReceiver receiver,
			String monitoringSet) {
		return subscribeToInUseLimits(receiver, monitoringSet, null);
	}
	
	@Override
	public RealTimeSnapshotSubscribedRequest subscribeToInUseLimits(RealTimeSnapshotSubscribedReceiver receiver,
			String monitoringSet, String resourceId) {

		RealTimeSnapshotSubscribedRequest subscription = new RealTimeSnapshotSubscribedRequest(
				httpClient, 
				host, 
				requestConfig, 
				bufferSize, 
				objectMapper,
				httpHeaders,
				realTimeRatingsPollMs,
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
			RealTimeSnapshotSubscribedReceiver receiver) {
		return subscribeToRegionalRealTimeLimits(receiver, null);
	}

	@Override
	public RegionalRealTimeSnapshotSubscribedRequest subscribeToRegionalRealTimeLimits(
			RealTimeSnapshotSubscribedReceiver receiver,
			String monitoringSet) {

		RegionalRealTimeSnapshotSubscribedRequest subscription = new RegionalRealTimeSnapshotSubscribedRequest(
				httpClient,
				host,
				requestConfig,
				bufferSize,
				objectMapper,
				httpHeaders,
				realTimeRatingsPollMs,
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
                                                                           String monitoringSet) {
		MonitoringSetsSubscribedRequest subscription = new MonitoringSetsSubscribedRequest(
				httpClient, host, requestConfig, monitoringSetPollMs, objectMapper, httpHeaders,
				monitoringSetPollMs, receiver, eTagStore, monitoringSet);
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
			MonitoringSetsSubscribedReceiver receiver) {
		var subscription = new DefaultMonitoringSetSubscribedRequest(
				httpClient, host, requestConfig, monitoringSetPollMs, objectMapper, httpHeaders,
				monitoringSetPollMs, receiver, eTagStore);
		addSubscription(subscription);
		return subscription;
	}

	@Override
	public void getInUseSeasonalSnapshots(SeasonalSnapshotReceiver receiver) {
		getInUseSeasonalSnapshots(receiver, null, null);
	}

	@Override
	public void getInUseSeasonalSnapshots(SeasonalSnapshotReceiver receiver, String monitoringSet) {
		getInUseSeasonalSnapshots(receiver, monitoringSet, null);
	}

	@Override
	public void getInUseSeasonalSnapshots(
			SeasonalSnapshotReceiver receiver,
			String monitoringSet,
			String resourceId) {

		new SeasonalSnapshotRequest(
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
	public SeasonalSnapshotSubscribedRequest subscribeToInUseSeasonalSnapshotUpdates(
			SeasonalSnapshotSubscribedReceiver receiver) {
		return subscribeToInUseSeasonalSnapshotUpdates(receiver, null);
	}

	@Override
	public SeasonalSnapshotSubscribedRequest subscribeToInUseSeasonalSnapshotUpdates(
			SeasonalSnapshotSubscribedReceiver receiver,
			String monitoringSet) {

		SeasonalSnapshotSubscribedRequest subscription = new SeasonalSnapshotSubscribedRequest(
				httpClient,
				host,
				requestConfig,
				bufferSize,
				objectMapper,
				httpHeaders,
				forecastRatingsPollMs,
				receiver,
				eTagStore,
				monitoringSet);

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
