package org.trolie.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.net.ServerSocketFactory;

import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.entity.GzipCompressingEntity;
import org.apache.hc.client5.http.entity.GzipDecompressingEntity;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.io.DefaultBHttpServerConnectionFactory;
import org.apache.hc.core5.http.impl.io.HttpService;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.protocol.DefaultHttpProcessor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trolie.client.model.common.DataProvenance;
import org.trolie.client.model.monitoringsets.MonitoringSet;
import org.trolie.client.model.operatingsnapshots.ForecastPeriodSnapshot;
import org.trolie.client.model.operatingsnapshots.ForecastSnapshotHeader;
import org.trolie.client.model.operatingsnapshots.RealTimeLimit;
import org.trolie.client.model.operatingsnapshots.RealTimeSnapshotHeader;
import org.trolie.client.model.ratingproposals.ForecastProposalHeader;
import org.trolie.client.model.ratingproposals.ForecastRatingPeriod;
import org.trolie.client.model.ratingproposals.ForecastRatingProposalStatus;
import org.trolie.client.model.ratingproposals.ProposalHeader;
import org.trolie.client.model.ratingproposals.RealTimeRating;
import org.trolie.client.model.ratingproposals.RealTimeRatingProposalStatus;
import org.trolie.client.request.monitoringsets.MonitoringSetsReceiver;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotReceiver;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscribedReceiver;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscribedRequest;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotReceiver;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotSubscribedReceiver;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotSubscribedRequest;
import org.trolie.client.request.ratingproposals.ForecastRatingProposalUpdate;
import org.trolie.client.request.ratingproposals.RealTimeRatingProposalUpdate;
import org.trolie.client.request.streaming.RequestSubscription;
import org.trolie.client.request.streaming.exception.StreamingGetException;
import org.trolie.client.util.TrolieApiConstants;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import static org.trolie.client.util.CommonConstants.TAG_SOURCE;
import static org.trolie.client.util.CommonConstants.TAG_ID;
import static org.trolie.client.util.CommonConstants.TAG_DESCRIPTION;
import static org.trolie.client.util.CommonConstants.TAG_POWER_SYSTEM_RESOURCES;

@Slf4j
public class TrolieClientTest {

	private static Logger logger = LoggerFactory.getLogger(TrolieClientTest.class);

	private static int PORT = 8080;
	private static String HOST = "http://127.0.0.1";
	private static String BASE_URI = HOST + ":" + PORT;

	static HttpServer httpServer;
	static Function<ClassicHttpRequest,ClassicHttpResponse> requestHandler;
	static ObjectMapper objectMapper;

	ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1,1,1,TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());

	@BeforeAll
	public static void createTestServer() throws Exception {

		objectMapper = new ObjectMapper();

		//create a simple HTTP server we can send requests to
		httpServer = new HttpServer(
				PORT, 
				HttpService.builder()
				.withHttpProcessor(new DefaultHttpProcessor(
						new HttpRequestInterceptor[0],
						new HttpResponseInterceptor[0]
						))
				.withHttpServerRequestHandler((request,trigger,context) -> {
					trigger.submitResponse(requestHandler.apply(request));
				})
				.build(),
				InetAddress.getLoopbackAddress(), 
				SocketConfig.DEFAULT, 
				ServerSocketFactory.getDefault(), 
				DefaultBHttpServerConnectionFactory.builder().build(), 
				null, 
				null);

		httpServer.start();

		//wait for test server to start up
		HttpClient startupCheckClient = HttpClientBuilder.create().build();		
		long now = System.currentTimeMillis();
		boolean started = false;
		requestHandler = r -> new BasicClassicHttpResponse(200);
		while (!started && System.currentTimeMillis() - now < 10000) {
			try {
				HttpGet get = new HttpGet(BASE_URI);
				BasicHttpClientResponseHandler handler = new BasicHttpClientResponseHandler();
				startupCheckClient.execute(get, handler);
				started = true;
			} catch (Exception e) {
				logger.info("Test server not started yet");
				Thread.sleep(1000);
			}
		}
		if (!started) {
			throw new IllegalStateException("Test server did not start within timeout");
		}
		logger.info("Test server started");
	}

	@AfterAll
	public static void cleanup() {
		if (httpServer != null) {
			httpServer.initiateShutdown();
		}
	}

	@Test
	public void testForecastRatingProposalStreamingUpdate() throws IOException {

		//test a roundtrip submission and response 

		String startTime = Instant.now().toString();

		requestHandler = request -> {

			ForecastRatingProposalStatus status = ForecastRatingProposalStatus.builder()
					.begins(startTime)
					.build();

			//we expect this request to be chunked
			Assertions.assertTrue(request.getEntity().isStreaming());
			Assertions.assertTrue(request.getEntity().isChunked());

			try (GzipDecompressingEntity entity = new GzipDecompressingEntity(request.getEntity())) {

				Map<String,Object> data = objectMapper.readValue(entity.getContent(),Map.class);
				List<Map<String,Object>> ratings = (List<Map<String,Object>>)data.get("ratings");
				Assertions.assertEquals(3, ratings.size());
				for (Map<String,Object> rating : ratings) {
					List<Map<String,Object>> periods = (List<Map<String,Object>>)rating.get("periods");
					Assertions.assertEquals(3, periods.size());
				}

				BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
				response.setEntity(new StringEntity(objectMapper.writeValueAsString(status)));
				return response;

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		HttpClientBuilder builder = HttpClientBuilder.create();

		try (TrolieClient trolieClient = new TrolieClientBuilder(BASE_URI,builder.build()).build();) {

			try (ForecastRatingProposalUpdate update = trolieClient.createForecastRatingProposalStreamingUpdate()) {

				ForecastProposalHeader header = ForecastProposalHeader.builder()
						.begins(startTime)
						.build();

				update.begin(header);
				for (int i=0;i<3;i++) {
					update.beginResource("resource" + i);
					for (int j=0;j<3;j++) {
						update.period(ForecastRatingPeriod.builder()
								.periodStart(startTime)
								.periodEnd(startTime)
								.continuousOperatingLimit(Map.of("mva",100F))
								.build());
					}
					update.endResource();
				}
				ForecastRatingProposalStatus status = update.complete();

				Assertions.assertEquals(startTime, status.getBegins());

			}
		}
	}

	@Test
	public void testForecastRatingProposalStreamingUpdate_ServerError() throws IOException {

		//make sure that server errors are clearly bubbled up with a status code

		String startTime = Instant.now().toString();

		requestHandler = request -> {
			BasicClassicHttpResponse response = new BasicClassicHttpResponse(500);
			return response;
		};

		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(BASE_URI,builder.build()).build();) {

			Assertions.assertThrows(TrolieServerException.class, () -> {
				try (ForecastRatingProposalUpdate update = trolieClient.createForecastRatingProposalStreamingUpdate()) {

					ForecastProposalHeader header = ForecastProposalHeader.builder()
							.begins(startTime)
							.build();

					update.begin(header);
					update.complete();
				}
			});
		}
	}

	@Test
	public void testForecastRatingProposalStreamingUpdate_ConnectError() throws IOException {

		//make sure that client I/O errors are clearly bubbled up

		String startTime = Instant.now().toString();

		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(HOST + ":" + 1111,builder.build()).build();) {

			try (ForecastRatingProposalUpdate update = trolieClient.createForecastRatingProposalStreamingUpdate()) {

				ForecastProposalHeader header = ForecastProposalHeader.builder()
						.begins(startTime)
						.build();

				//must create enough data to fill the buffer for this to be a good test so
				//we can make sure stream is not jammed up by death of request execution thread
				//and no bytes being taken off the buffer
				update.begin(header);
				for (int i=0;i<100;i++) {
					update.beginResource("resource" + i);
					for (int j=0;j<10;j++) {
						update.period(ForecastRatingPeriod.builder()
								.periodStart(startTime)
								.periodEnd(startTime)
								.continuousOperatingLimit(Map.of("mva",100F))
								.build());
					}
					update.endResource();
				}
				update.complete();
			} catch (TrolieException e) {
				Assertions.assertEquals(HttpHostConnectException.class, e.getCause().getClass());
			}
		}
	}

	@Test
	public void testForecastSnapshotSubscription() throws Exception {

		//we will run the subscription for fixed number of requests
		AtomicInteger requestCounter = new AtomicInteger(0);
		String startTime = Instant.now().toString();
		String etag = UUID.randomUUID().toString();

		requestHandler = request -> {

			BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);

			try {

				//we expect to get the configured monitoring set name as a query param
				Assertions.assertEquals("monitoring-set=abc", request.getUri().getQuery());

				Header requestEtag = request.getHeader(HttpHeaders.IF_NONE_MATCH);

				//2nd+ request should have an etag header 
				if (requestCounter.get() > 0) {
					Assertions.assertNotNull(requestEtag);
					Assertions.assertEquals(etag, requestEtag.getValue());
				}

				if (requestCounter.get() == 3) {

					//on 4th request return error to test error propagation to receiver
					response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);

				} else if (requestCounter.get() % 2 == 0) {

					//on 1st and 3rd request, return a new snapshot to indicate an update
					PipedOutputStream out = new PipedOutputStream();
					PipedInputStream in = new PipedInputStream(out);

					response.setHeader(HttpHeaders.ETAG, etag);
					response.setEntity(
							new GzipCompressingEntity(new InputStreamEntity(in,ContentType.create(TrolieApiConstants.CONTENT_TYPE_FORECAST_SNAPSHOT))));

					threadPoolExecutor.submit(new Callable<Void>() {
						@Override
						public Void call() throws Exception {

							try (JsonGenerator json = new JsonFactory(objectMapper).createGenerator(out)) {
								writeForecastSnapshot(json, startTime);
								return null;
							} catch (Exception e) {
								e.printStackTrace();
								throw new RuntimeException(e);
							}
						}

					});

				} else {

					//on 2nd request, indicate existing etag is valid to test that request is short-circuited
					response.setCode(HttpStatus.SC_NOT_MODIFIED);
				}

			} catch (Exception e) {
				e.printStackTrace();
				response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			} finally {
				requestCounter.incrementAndGet();
			}

			return response;
		};


		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(BASE_URI,builder.build()).build();) {

			AtomicInteger snapshotsReceived = new AtomicInteger(0);
			AtomicInteger errorCount = new AtomicInteger(0);

			//subscribe for snapshots and validate they are transmitted correctly
			ForecastSnapshotSubscribedRequest subscription = trolieClient.subscribeToInUseLimitForecastUpdates(new ForecastSnapshotSubscribedReceiver() {

				RequestSubscription subscription;
				int numResources;
				int numPeriods;

				@Override
				public void period(ForecastPeriodSnapshot period) {
					numPeriods++;
				}

				@Override
				public void header(ForecastSnapshotHeader header) {
					Assertions.assertEquals(startTime, header.getBegins());
				}

				@Override
				public void endSnapshot() {
					Assertions.assertEquals(100, numResources);
					numResources = 0;
				}

				@Override
				public void endResource() {
					Assertions.assertEquals(24, numPeriods);
					numPeriods = 0;
				}

				@Override
				public void beginSnapshot() {
					snapshotsReceived.incrementAndGet();
				}

				@Override
				public void beginResource(String resourceId) {
					numResources++;
				}

				@Override
				public void error(StreamingGetException t) {
					errorCount.incrementAndGet();
					subscription.stop();
				}

				@Override
				public void setSubscription(RequestSubscription subscription) {
					this.subscription = subscription;
				}


			}, "abc", 1000);

			while (subscription.isSubscribed()) {
				Thread.sleep(100);
			}

			//we should have received 2 snapshots, 1 304 code and 1 500 code
			Assertions.assertEquals(2, snapshotsReceived.get());
			Assertions.assertEquals(1, errorCount.get());
		}
	}


	@Test
	public void testRealTimeRatingProposalStreamingUpdate() throws IOException {

		//test a roundtrip submission and response 

		requestHandler = request -> {

			RealTimeRatingProposalStatus status = RealTimeRatingProposalStatus.builder()
					.incompleteObligationCount(5)
					.build();

			//we expect this request to be chunked
			Assertions.assertTrue(request.getEntity().isStreaming());
			Assertions.assertTrue(request.getEntity().isChunked());

			try (GzipDecompressingEntity entity = new GzipDecompressingEntity(request.getEntity())) {

				Map<String,Object> data = objectMapper.readValue(entity.getContent(),Map.class);
				List<Map<String,Object>> ratings = (List<Map<String,Object>>)data.get("ratings");
				Assertions.assertEquals(3, ratings.size());

				BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
				response.setEntity(new StringEntity(objectMapper.writeValueAsString(status)));
				return response;

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(BASE_URI,builder.build()).build();) {

			try (RealTimeRatingProposalUpdate update = trolieClient.createRealTimeRatingProposalStreamingUpdate()) {

				ProposalHeader header = ProposalHeader.builder()
						.build();

				update.begin(header);
				for (int i=0;i<3;i++) {
					update.rating(RealTimeRating.builder().continuousOperatingLimit(Map.of("MVA",100f)).build());
				}
				RealTimeRatingProposalStatus status = update.complete();

				Assertions.assertEquals(5, status.getIncompleteObligationCount());

			}
		}
	}

	@Test
	public void testRealTimeSnapshotSubscription() throws Exception {

		//we will run the subscription for fixed number of requests
		AtomicInteger requestCounter = new AtomicInteger(0);
		String etag = UUID.randomUUID().toString();

		requestHandler = request -> {

			BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);

			try {

				//we expect to get the configured monitoring set name as a query param
				Assertions.assertEquals(
						TrolieApiConstants.PARAM_MONITORING_SET + "=abc&" + TrolieApiConstants.PARAM_TRANSMISSION_FACILITY + "=xyz", 
						request.getUri().getQuery());

				Header requestEtag = request.getHeader(HttpHeaders.IF_NONE_MATCH);

				//2nd+ request should have an etag header 
				if (requestCounter.get() > 0) {
					Assertions.assertNotNull(requestEtag);
					Assertions.assertEquals(etag, requestEtag.getValue());
				}

				if (requestCounter.get() == 3) {

					//on 4th request return error to test error propagation to receiver
					response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);

				} else if (requestCounter.get() % 2 == 0) {

					//on 1st and 3rd request, return a new snapshot to indicate an update
					PipedOutputStream out = new PipedOutputStream();
					PipedInputStream in = new PipedInputStream(out);

					response.setHeader(HttpHeaders.ETAG, etag);
					response.setEntity(
							new GzipCompressingEntity(new InputStreamEntity(in,ContentType.create(TrolieApiConstants.CONTENT_TYPE_REALTIME_SNAPSHOT))));

					threadPoolExecutor.submit(new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							try (JsonGenerator json = new JsonFactory(objectMapper).createGenerator(out)) {
								writeRealTimeSnapshot(json);
								return null;
							} catch (Exception e) {
								e.printStackTrace();
								throw new RuntimeException(e);
							}
						}

					});

				} else {

					//on 2nd request, indicate existing etag is valid to test that request is short-circuited
					response.setCode(HttpStatus.SC_NOT_MODIFIED);
				}

			} catch (Exception e) {
				e.printStackTrace();
				response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			} finally {
				requestCounter.incrementAndGet();
			}

			return response;
		};


		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(BASE_URI,builder.build()).build();) {

			AtomicInteger snapshotsReceived = new AtomicInteger(0);
			AtomicInteger errorCount = new AtomicInteger(0);

			//subscribe for snapshots and validate they are transmitted correctly
			RealTimeSnapshotSubscribedRequest subscription = trolieClient.subscribeToInUseLimits(new RealTimeSnapshotSubscribedReceiver() {

				RequestSubscription subscription;
				int numResources;

				@Override
				public void header(RealTimeSnapshotHeader header) {
					Assertions.assertNotNull(header);
				}

				@Override
				public void limit(RealTimeLimit limit) {
					numResources++;
				}


				@Override
				public void endSnapshot() {
					Assertions.assertEquals(100, numResources);
					numResources = 0;
				}

				@Override
				public void beginSnapshot() {
					snapshotsReceived.incrementAndGet();
				}

				@Override
				public void error(StreamingGetException t) {
					errorCount.incrementAndGet();
					subscription.stop();
				}

				@Override
				public void setSubscription(RequestSubscription subscription) {
					this.subscription = subscription;
				}


			}, "abc", "xyz", 1000);

			while (subscription.isSubscribed()) {
				Thread.sleep(100);
			}

			//we should have received 2 snapshots, 1 304 code and 1 500 code
			Assertions.assertEquals(2, snapshotsReceived.get());
			Assertions.assertEquals(1, errorCount.get());
		}
	}



	@Test
	public void testRealTimeSnapshotGet() throws Exception {

		requestHandler = request -> {

			BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);

			try {

				//we expect to get the configured monitoring set name as a query param
				Assertions.assertEquals(
						TrolieApiConstants.PARAM_MONITORING_SET + "=abc&" + TrolieApiConstants.PARAM_TRANSMISSION_FACILITY + "=xyz", 
						request.getUri().getQuery());

				//on 1st and 3rd request, return a new snapshot to indicate an update
				PipedOutputStream out = new PipedOutputStream();
				PipedInputStream in = new PipedInputStream(out);

				response.setEntity(
						new GzipCompressingEntity(new InputStreamEntity(in,ContentType.create(TrolieApiConstants.CONTENT_TYPE_REALTIME_SNAPSHOT))));

				threadPoolExecutor.submit(new Callable<Void>() {
					@Override
					public Void call() throws Exception {

						try (JsonGenerator json = new JsonFactory(objectMapper).createGenerator(out)) {


							writeRealTimeSnapshot(json);

							return null;
						} catch (Exception e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}
					}
				});

			} catch (Exception e) {
				e.printStackTrace();
				response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}

			return response;
		};


		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(BASE_URI,builder.build()).build();) {

			AtomicInteger snapshotsReceived = new AtomicInteger(0);
			AtomicInteger errorCount = new AtomicInteger(0);

			//subscribe for snapshots and validate they are transmitted correctly
			trolieClient.getInUseLimits(new RealTimeSnapshotReceiver() {

				int numResources;

				@Override
				public void header(RealTimeSnapshotHeader header) {
					Assertions.assertNotNull(header);
				}

				@Override
				public void limit(RealTimeLimit limit) {
					numResources++;
				}


				@Override
				public void endSnapshot() {
					Assertions.assertEquals(100, numResources);
					numResources = 0;
				}

				@Override
				public void beginSnapshot() {
					snapshotsReceived.incrementAndGet();
				}

				@Override
				public void error(StreamingGetException t) {
					errorCount.incrementAndGet();
				}


			}, "abc", "xyz");

			Assertions.assertEquals(1, snapshotsReceived.get());
			Assertions.assertEquals(0, errorCount.get());
		}
	}


	@Test
	public void testForecastSnapshotGet() throws Exception {

		Instant startTime = Instant.now();
		String startTimeString = startTime.toString();

		requestHandler = request -> {

			BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);

			try {

				//we expect to get the monitoring set name and period start/end as a query params
				Assertions.assertEquals(
						TrolieApiConstants.PARAM_MONITORING_SET + "=abc&" + 
								TrolieApiConstants.PARAM_OFFSET_PERIOD_START + "=" + startTimeString + "&" +
								TrolieApiConstants.PARAM_PERIOD_END + "=" + startTimeString,
								request.getUri().getQuery());

				//on 1st and 3rd request, return a new snapshot to indicate an update
				PipedOutputStream out = new PipedOutputStream();
				PipedInputStream in = new PipedInputStream(out);

				response.setEntity(
						new GzipCompressingEntity(new InputStreamEntity(in,ContentType.create(TrolieApiConstants.CONTENT_TYPE_FORECAST_SNAPSHOT))));

				threadPoolExecutor.submit(new Callable<Void>() {
					@Override
					public Void call() throws Exception {

						try (JsonGenerator json = new JsonFactory(objectMapper).createGenerator(out)) {

							writeForecastSnapshot(json, startTimeString);

							return null;
						} catch (Exception e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}
					}
				});

			} catch (Exception e) {
				e.printStackTrace();
				response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}

			return response;
		};


		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(BASE_URI,builder.build()).build();) {

			AtomicInteger snapshotsReceived = new AtomicInteger(0);
			AtomicInteger errorCount = new AtomicInteger(0);

			//subscribe for snapshots and validate they are transmitted correctly
			trolieClient.getInUseLimitForecasts(new ForecastSnapshotReceiver() {

				int numResources;
				int numPeriods;

				@Override
				public void header(ForecastSnapshotHeader header) {
					Assertions.assertNotNull(header);
					Assertions.assertEquals(startTimeString, header.getBegins());
				}


				@Override
				public void endSnapshot() {
					Assertions.assertEquals(100, numResources);
					numResources = 0;
				}

				@Override
				public void beginSnapshot() {
					snapshotsReceived.incrementAndGet();
				}

				@Override
				public void error(StreamingGetException t) {
					errorCount.incrementAndGet();
				}


				@Override
				public void beginResource(String resourceId) {
					numResources++;
				}


				@Override
				public void period(ForecastPeriodSnapshot period) {
					numPeriods++;
				}


				@Override
				public void endResource() {
					Assertions.assertEquals(24, numPeriods);
					numPeriods = 0;
				}


			}, "abc", startTime, startTime);

			Assertions.assertEquals(1, snapshotsReceived.get());
			Assertions.assertEquals(0, errorCount.get());

		}
	}
	

	@Test
	public void testMonitoringSetsGet() throws Exception {
		String id = "monitoring-set";
		requestHandler = request -> {
			BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
			try {
				PipedOutputStream out = new PipedOutputStream();
				PipedInputStream in = new PipedInputStream(out);
				response.setEntity(
						new GzipCompressingEntity(new InputStreamEntity(in, ContentType.create(TrolieApiConstants.CONTENT_TYPE_MONITORING_SET))));
				threadPoolExecutor.submit(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						try (JsonGenerator json = new JsonFactory(objectMapper).createGenerator(out)) {
							writeMonitoringSet(json, id);
							return null;
						} catch (Exception e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}
					}
				});

			} catch (Exception e) {
				e.printStackTrace();
				response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}

			return response;
		};

		HttpClientBuilder builder = HttpClientBuilder.create();
		TrolieClient trolieClient = new TrolieClientBuilder(BASE_URI, builder).build();
		AtomicInteger receivedCount = new AtomicInteger(0);
		AtomicInteger errorCount = new AtomicInteger(0);
		//subscribe for snapshots and validate they are transmitted correctly
		trolieClient.getMonitoringSet(new MonitoringSetsReceiver() {

			@Override
			public void error(StreamingGetException t) {
				errorCount.incrementAndGet();
			}

			@Override
			public void header(MonitoringSet monitoringSet) {
				assertNotNull(monitoringSet);
				assertEquals(id, monitoringSet.getId());
				assertNotNull(monitoringSet.getDescription());
				assertNotNull(monitoringSet.getSource());
				assertNotNull(monitoringSet.getPowerSystemResources());
			}

			@Override
			public void end() {
			}

			@Override
			public void begin() {
				receivedCount.incrementAndGet();
			}
		}, id);
		Assertions.assertEquals(1, receivedCount.get());
		Assertions.assertEquals(0, errorCount.get());
	}

	@Test
	public void testDefaultMonitoringSetsGet() throws Exception {
		String id = "def-monitoring-set";
		requestHandler = request -> {
			BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
			try {
				PipedOutputStream out = new PipedOutputStream();
				PipedInputStream in = new PipedInputStream(out);
				response.setEntity(
						new GzipCompressingEntity(new InputStreamEntity(in, ContentType.create(TrolieApiConstants.CONTENT_TYPE_MONITORING_SET))));
				threadPoolExecutor.submit(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						try (JsonGenerator json = new JsonFactory(objectMapper).createGenerator(out)) {
							writeMonitoringSet(json, id);
							return null;
						} catch (Exception e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}
					}
				});

			} catch (Exception e) {
				e.printStackTrace();
				response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}

			return response;
		};

		HttpClientBuilder builder = HttpClientBuilder.create();
		TrolieClient trolieClient = new TrolieClientBuilder(BASE_URI, builder).build();
		AtomicInteger receivedCount = new AtomicInteger(0);
		AtomicInteger errorCount = new AtomicInteger(0);
		//subscribe for snapshots and validate they are transmitted correctly
		trolieClient.getDefaultMonitoringSet(new MonitoringSetsReceiver() {

			@Override
			public void error(StreamingGetException t) {
				errorCount.incrementAndGet();
			}

			@Override
			public void header(MonitoringSet monitoringSet) {
				assertNotNull(monitoringSet);
				assertEquals(id, monitoringSet.getId());
				assertNotNull(monitoringSet.getDescription());
				assertNotNull(monitoringSet.getSource());
				assertNotNull(monitoringSet.getPowerSystemResources());
			}

			@Override
			public void end() {
			}

			@Override
			public void begin() {
				receivedCount.incrementAndGet();
			}
		}, id);
		Assertions.assertEquals(1, receivedCount.get());
		Assertions.assertEquals(0, errorCount.get());
	}

	private void writeMonitoringSet(JsonGenerator json, String id) throws IOException {
		var source = DataProvenance.builder().provider(id).lastUpdated(
				Instant.now().toString()).originId(id).build();
		MonitoringSet monitoringSet = new MonitoringSet(source, id, "This is test SDK", List.of());
		json.writeStartObject();
		try {
			json.writeFieldName(TAG_SOURCE);
			json.writeObject(monitoringSet.getSource());
			json.writeFieldName(TAG_ID);
			json.writeObject(monitoringSet.getId());
			json.writeFieldName(TAG_DESCRIPTION);
			json.writeObject(monitoringSet.getDescription());
			json.writeFieldName(TAG_POWER_SYSTEM_RESOURCES);
			json.writeObject(monitoringSet.getPowerSystemResources());
		}catch (Exception e) {
			log.error("writeMonitoringSet.error ", e);
		}
		json.writeEndObject();
	}

	private void writeForecastSnapshot(JsonGenerator json, String startTime) throws IOException {

		ForecastSnapshotHeader header = new ForecastSnapshotHeader(startTime);

		json.writeStartObject();

		json.writeFieldName("snapshot-header");
		json.writeObject(header);

		json.writeFieldName("ratings");
		json.writeStartArray();

		for (int i=0;i<100;i++) {
			json.writeStartObject();
			json.writeFieldName("resource-id");
			json.writeString("resource" + i);
			json.writeFieldName("periods");						
			json.writeStartArray();
			for (int j=0;j<24;j++) {
				ForecastPeriodSnapshot period = new ForecastPeriodSnapshot(
						startTime,
						startTime,
						Map.of("mva",100F),
						Collections.emptyList()
						);
				json.writeObject(period);
			}
			json.writeEndArray();
			json.writeEndObject();
		}

		json.writeEndArray();
		json.writeEndObject();

	}


	private void writeRealTimeSnapshot(JsonGenerator json) throws IOException {
		json.writeStartObject();

		RealTimeSnapshotHeader header = new RealTimeSnapshotHeader();
		json.writeFieldName("snapshot-header");
		json.writeObject(header);

		json.writeFieldName("ratings");
		json.writeStartArray();

		for (int i=0;i<100;i++) {
			json.writeObject(RealTimeLimit.builder()
					.resourceId("resource" + i)
					.continuousOperatingLimit(Map.of("mva",100f)).build());
		}

		json.writeEndArray();
		json.writeEndObject();
	}
}
