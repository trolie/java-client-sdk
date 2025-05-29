package energy.trolie.client;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import energy.trolie.client.exception.StreamingGetException;
import energy.trolie.client.exception.TrolieException;
import energy.trolie.client.exception.TrolieServerException;
import energy.trolie.client.impl.request.RequestSubscriptionInternal;
import energy.trolie.client.model.common.DataProvenance;
import energy.trolie.client.model.common.RatingValue;
import energy.trolie.client.model.monitoringsets.MonitoringSet;
import energy.trolie.client.model.operatingsnapshots.ForecastPeriodSnapshot;
import energy.trolie.client.model.operatingsnapshots.ForecastSnapshotHeader;
import energy.trolie.client.model.operatingsnapshots.RealTimeLimit;
import energy.trolie.client.model.operatingsnapshots.RealTimeSnapshotHeader;
import energy.trolie.client.model.ratingproposals.ForecastProposalHeader;
import energy.trolie.client.model.ratingproposals.ForecastRatingPeriod;
import energy.trolie.client.model.ratingproposals.ForecastRatingProposalStatus;
import energy.trolie.client.model.ratingproposals.ProposalHeader;
import energy.trolie.client.model.ratingproposals.RealTimeRating;
import energy.trolie.client.model.ratingproposals.RealTimeRatingProposalStatus;
import energy.trolie.client.request.monitoringsets.MonitoringSetsReceiver;
import energy.trolie.client.request.monitoringsets.MonitoringSetsSubscribedReceiver;
import energy.trolie.client.request.operatingsnapshots.ForecastSnapshotReceiver;
import energy.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscribedReceiver;
import energy.trolie.client.request.operatingsnapshots.RealTimeSnapshotReceiver;
import energy.trolie.client.request.operatingsnapshots.RealTimeSnapshotSubscribedReceiver;
import energy.trolie.client.request.ratingproposals.ForecastRatingProposalUpdate;
import energy.trolie.client.request.ratingproposals.RealTimeRatingProposalUpdate;
import lombok.extern.slf4j.Slf4j;
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

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SuppressWarnings("unchecked")
public class TrolieClientIT {

	public static final String TAG_SOURCE = "source";
	public static final String TAG_ID = "id";
	public static final String TAG_DESCRIPTION = "description";
	public static final String TAG_POWER_SYSTEM_RESOURCES = "power-system-resources";

	private static final String HOST = "http://127.0.0.1";
	private static String baseUri;

	static HttpServer httpServer;
	static Function<ClassicHttpRequest,ClassicHttpResponse> requestHandler;
	static ObjectMapper objectMapper;

	ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1,1,1,TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());

	@BeforeAll
	public static void createTestServer() throws Exception {

		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		int port;
		try (ServerSocket serverSocket = new ServerSocket(0)) {
			port = serverSocket.getLocalPort();
        }

		baseUri = HOST + ":" + port;

		//create a simple HTTP server we can send requests to
		httpServer = new HttpServer(
				port,
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
				HttpGet get = new HttpGet(baseUri);
				BasicHttpClientResponseHandler handler = new BasicHttpClientResponseHandler();
				startupCheckClient.execute(get, handler);
				started = true;
			} catch (Exception e) {
				log.info("Test server not started yet");
				Thread.sleep(1000);
			}
		}
		if (!started) {
			throw new IllegalStateException("Test server did not start within timeout");
		}
		log.info("Test server started");
	}

	@AfterAll
	public static void cleanup() {
		if (httpServer != null) {
			httpServer.initiateShutdown();
		}
	}

	@Test
	void testForecastRatingProposalStreamingUpdate() throws IOException {

		//test a roundtrip submission and response 

		var startTime = Instant.now();

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

		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri + "/test-path", builder.build()).build()) {

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
								.continuousOperatingLimit(RatingValue.fromMva(100f))
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
	void testForecastRatingProposalStreamingUpdate_ServerError() throws IOException {

		//make sure that server errors are clearly bubbled up with a status code

		var startTime = Instant.now();

		requestHandler = request -> {
			BasicClassicHttpResponse response = new BasicClassicHttpResponse(500);
			return response;
		};

		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri,builder.build()).build();) {

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
	void testForecastRatingProposalStreamingUpdate_ConnectError() throws IOException {

		//make sure that client I/O errors are clearly bubbled up

		var startTime = Instant.now();

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
								.continuousOperatingLimit(RatingValue.fromMva(100f))
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
	void testForecastSnapshotGet() throws Exception {

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
				response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
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

			} catch (Exception e) {
				e.printStackTrace();
				response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}

			return response;
		};


		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri,builder.build()).build();) {

			AtomicInteger snapshotsReceived = new AtomicInteger(0);
			AtomicInteger errorCount = new AtomicInteger(0);

			//subscribe for snapshots and validate they are transmitted correctly
			trolieClient.getInUseLimitForecasts(new ForecastSnapshotReceiver() {

				int numResources;
				int numPeriods;

				@Override
				public void header(ForecastSnapshotHeader header) {
					Assertions.assertNotNull(header);
					Assertions.assertEquals(startTime, header.getBegins());
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


			}, "abc", null, startTime, startTime);

			Assertions.assertEquals(1, snapshotsReceived.get());
			Assertions.assertEquals(0, errorCount.get());

		}
	}

	@Test
	void testForecastSnapshotSubscription() throws Exception {

		//we will run the subscription for fixed number of requests
		AtomicInteger requestCounter = new AtomicInteger(0);
		Instant startTime = Instant.now();
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
					response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
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
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri,builder.build())
				.forecastRatingsPollMs(200)
				.build();) {

			AtomicInteger snapshotsReceived = new AtomicInteger(0);
			AtomicInteger errorCount = new AtomicInteger(0);

			//subscribe for snapshots and validate they are transmitted correctly
			var subscription = trolieClient.subscribeToInUseLimitForecastUpdates(new ForecastSnapshotSubscribedReceiver() {

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
					((RequestSubscriptionInternal)subscription).stop();
				}

				@Override
				public void setSubscription(RequestSubscription subscription) {
					this.subscription = subscription;
				}


			}, "abc");

			while (subscription.isSubscribed()) {
				Thread.sleep(100);
				assertTrue(subscription.isHealthy());
			}

			//we should have received 2 snapshots, 1 304 code and 1 500 code
			Assertions.assertEquals(2, snapshotsReceived.get());
			Assertions.assertEquals(1, errorCount.get());
		}
	}

	@Test
	void testRegionalForecastSnapshotGet() throws Exception {

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
				response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
				threadPoolExecutor.submit((Callable<Void>) () -> {

                    try (JsonGenerator json = new JsonFactory(objectMapper).createGenerator(out)) {

                        writeForecastSnapshot(json, startTime);

                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });

			} catch (Exception e) {
				e.printStackTrace();
				response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}

			return response;
		};


		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri,builder.build()).build();) {

			AtomicInteger snapshotsReceived = new AtomicInteger(0);
			AtomicInteger errorCount = new AtomicInteger(0);

			//subscribe for snapshots and validate they are transmitted correctly
			trolieClient.getRegionalLimitsForecast(new ForecastSnapshotReceiver() {

				int numResources;
				int numPeriods;

				@Override
				public void header(ForecastSnapshotHeader header) {
					Assertions.assertNotNull(header);
					Assertions.assertEquals(startTime, header.getBegins());
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


			}, "abc", null, startTime, startTime);

			Assertions.assertEquals(1, snapshotsReceived.get());
			Assertions.assertEquals(0, errorCount.get());

		}
	}

	@Test
	void testRegionalForecastSnapshotSubscription() throws Exception {

		//we will run the subscription for fixed number of requests
		AtomicInteger requestCounter = new AtomicInteger(0);
		Instant startTime = Instant.now();
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
					response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
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
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri,builder.build())
				.forecastRatingsPollMs(200).build();) {

			AtomicInteger snapshotsReceived = new AtomicInteger(0);
			AtomicInteger errorCount = new AtomicInteger(0);

			//subscribe for snapshots and validate they are transmitted correctly
			var subscription = trolieClient.subscribeToRegionalLimitsForecast(new ForecastSnapshotSubscribedReceiver() {

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
					((RequestSubscriptionInternal)subscription).stop();
				}

				@Override
				public void setSubscription(RequestSubscription subscription) {
					this.subscription = subscription;
				}


			}, "abc");

			while (subscription.isSubscribed()) {
				Thread.sleep(100);
			}

			//we should have received 2 snapshots, 1 304 code and 1 500 code
			Assertions.assertEquals(2, snapshotsReceived.get());
			Assertions.assertEquals(1, errorCount.get());
		}
	}

	@Test
	void testRealTimeRatingProposalStreamingUpdate() throws IOException {

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
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri,builder.build()).build();) {

			try (RealTimeRatingProposalUpdate update = trolieClient.createRealTimeRatingProposalStreamingUpdate()) {

				ProposalHeader header = ProposalHeader.builder()
						.build();

				update.begin(header);
				for (int i=0;i<3;i++) {
					update.rating(RealTimeRating.builder().continuousOperatingLimit(RatingValue.fromMva(100f)).build());
				}
				RealTimeRatingProposalStatus status = update.complete();

				Assertions.assertEquals(5, status.getIncompleteObligationCount());

			}
		}
	}

	@Test
	void testRealTimeSnapshotSubscription() throws Exception {

		//we will run the subscription for fixed number of requests
		AtomicInteger requestCounter = new AtomicInteger(0);
		String etag = UUID.randomUUID().toString();

		requestHandler = request -> {

			BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);

			try {

				//we expect to get the configured monitoring set name as a query param
				Assertions.assertEquals(
						TrolieApiConstants.PARAM_MONITORING_SET + "=abc&" + TrolieApiConstants.PARAM_RESOURCE_ID + "=xyz",
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
					response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
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
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri,builder.build())
				.realTimeRatingsPollMs(200)
				.build();) {

			AtomicInteger snapshotsReceived = new AtomicInteger(0);
			AtomicInteger errorCount = new AtomicInteger(0);

			//subscribe for snapshots and validate they are transmitted correctly
			var subscription = trolieClient.subscribeToInUseLimits(new RealTimeSnapshotSubscribedReceiver() {

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
					((RequestSubscriptionInternal)subscription).stop();
				}

				@Override
				public void setSubscription(RequestSubscription subscription) {
					this.subscription = subscription;
				}


			}, "abc", "xyz");

			while (subscription.isSubscribed()) {
				Thread.sleep(100);
			}

			//we should have received 2 snapshots, 1 304 code and 1 500 code
			Assertions.assertEquals(2, snapshotsReceived.get());
			Assertions.assertEquals(1, errorCount.get());
		}
	}

	@Test
	void testRealTimeSnapshotGet() throws Exception {

		requestHandler = request -> {

			BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);

			try {

				//we expect to get the configured monitoring set name as a query param
				Assertions.assertEquals(
						TrolieApiConstants.PARAM_MONITORING_SET + "=abc&" + TrolieApiConstants.PARAM_RESOURCE_ID + "=xyz",
						request.getUri().getQuery());

				//on 1st and 3rd request, return a new snapshot to indicate an update
				PipedOutputStream out = new PipedOutputStream();
				PipedInputStream in = new PipedInputStream(out);

				response.setEntity(
						new GzipCompressingEntity(new InputStreamEntity(in,ContentType.create(TrolieApiConstants.CONTENT_TYPE_REALTIME_SNAPSHOT))));
				response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
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
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri,builder.build()).build();) {

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
	void testRegionalRealTimeSnapshotSubscription() throws Exception {

		//we will run the subscription for fixed number of requests
		AtomicInteger requestCounter = new AtomicInteger(0);
		String etag = UUID.randomUUID().toString();

		requestHandler = request -> {

			BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);

			try {

				//we expect to get the configured monitoring set name as a query param
				Assertions.assertEquals(
						TrolieApiConstants.PARAM_MONITORING_SET + "=abc",
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
					response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
					threadPoolExecutor.submit((Callable<Void>) () -> {
                        try (JsonGenerator json = new JsonFactory(objectMapper).createGenerator(out)) {
                            writeRealTimeSnapshot(json);
                            return null;
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
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
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri,builder.build())
				.realTimeRatingsPollMs(200)
				.build();) {

			AtomicInteger snapshotsReceived = new AtomicInteger(0);
			AtomicInteger errorCount = new AtomicInteger(0);

			//subscribe for snapshots and validate they are transmitted correctly
			var subscription = trolieClient.subscribeToRegionalRealTimeLimits(new RealTimeSnapshotSubscribedReceiver() {

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
					((RequestSubscriptionInternal)subscription).stop();
				}

				@Override
				public void setSubscription(RequestSubscription subscription) {
					this.subscription = subscription;
				}


			}, "abc");

			while (subscription.isSubscribed()) {
				Thread.sleep(100);
			}

			//we should have received 2 snapshots, 1 304 code and 1 500 code
			Assertions.assertEquals(2, snapshotsReceived.get());
			Assertions.assertEquals(1, errorCount.get());
		}
	}

	@Test
	void testRegionalRealTimeSnapshotGet() throws Exception {

		requestHandler = request -> {

			BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);

			try {

				//we expect to get the configured monitoring set name as a query param
				Assertions.assertEquals(
						TrolieApiConstants.PARAM_MONITORING_SET + "=abc&" + TrolieApiConstants.PARAM_RESOURCE_ID + "=xyz",
						request.getUri().getQuery());

				//on 1st and 3rd request, return a new snapshot to indicate an update
				PipedOutputStream out = new PipedOutputStream();
				PipedInputStream in = new PipedInputStream(out);

				response.setEntity(
						new GzipCompressingEntity(new InputStreamEntity(in,ContentType.create(TrolieApiConstants.CONTENT_TYPE_REALTIME_SNAPSHOT))));
				response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
				threadPoolExecutor.submit((Callable<Void>) () -> {

                    try (JsonGenerator json = new JsonFactory(objectMapper).createGenerator(out)) {


                        writeRealTimeSnapshot(json);

                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });

			} catch (Exception e) {
				e.printStackTrace();
				response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}

			return response;
		};


		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri,builder.build()).build();) {

			AtomicInteger snapshotsReceived = new AtomicInteger(0);
			AtomicInteger errorCount = new AtomicInteger(0);

			// Get a snapshots and validate it is transmitted correctly
			trolieClient.getRegionalRealTimeLimits(new RealTimeSnapshotReceiver() {

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
	void testMonitoringSetsGet() {
		String id = "monitoring-set";
		requestHandler = request -> {
			BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
			try {
				PipedOutputStream out = new PipedOutputStream();
				PipedInputStream in = new PipedInputStream(out);
				response.setEntity(new GzipCompressingEntity(new InputStreamEntity(in,
						ContentType.create(TrolieApiConstants.CONTENT_TYPE_MONITORING_SET))));
				response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
				threadPoolExecutor.submit((Callable<Void>) () -> {
                    try (JsonGenerator json = new JsonFactory(objectMapper).createGenerator(out)) {
                        writeMonitoringSet(json, id);
                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });

			} catch (Exception e) {
				e.printStackTrace();
				response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}

			return response;
		};

		HttpClientBuilder builder = HttpClientBuilder.create();
		TrolieClient trolieClient = new TrolieClientBuilder(baseUri, builder.build()).build();
		AtomicInteger receivedCount = new AtomicInteger(0);
		AtomicInteger errorCount = new AtomicInteger(0);
		//subscribe for monitoring sets and validate they are transmitted correctly
		trolieClient.getMonitoringSet(new MonitoringSetsReceiver() {

			@Override
			public void error(StreamingGetException t) {
				errorCount.incrementAndGet();
			}

			@Override
			public void monitoringSet(MonitoringSet monitoringSet) {
				receivedCount.incrementAndGet();
				assertNotNull(monitoringSet);
				assertEquals(id, monitoringSet.getId());
				assertNotNull(monitoringSet.getDescription());
				assertNotNull(monitoringSet.getSource());
				assertNotNull(monitoringSet.getPowerSystemResources());
			}
		}, id);
		Assertions.assertEquals(1, receivedCount.get());
		Assertions.assertEquals(0, errorCount.get());
	}

	@Test
	void testDefaultMonitoringSetsGet() {
		String id = "def-monitoring-set";
		requestHandler = request -> {
			BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
			try {
				PipedOutputStream out = new PipedOutputStream();
				PipedInputStream in = new PipedInputStream(out);
				response.setEntity(
						new GzipCompressingEntity(new InputStreamEntity(in, ContentType.create(TrolieApiConstants.CONTENT_TYPE_MONITORING_SET))));
				response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
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
		TrolieClient trolieClient = new TrolieClientBuilder(baseUri, builder.build())
				.build();
		AtomicInteger receivedCount = new AtomicInteger(0);
		AtomicInteger errorCount = new AtomicInteger(0);
		//subscribe for snapshots and validate they are transmitted correctly
		trolieClient.getDefaultMonitoringSet(new MonitoringSetsReceiver() {

			@Override
			public void error(StreamingGetException t) {
				errorCount.incrementAndGet();
			}

			@Override
			public void monitoringSet(MonitoringSet monitoringSet) {
				receivedCount.incrementAndGet();
				assertNotNull(monitoringSet);
				assertEquals(id, monitoringSet.getId());
				assertNotNull(monitoringSet.getDescription());
				assertNotNull(monitoringSet.getSource());
				assertNotNull(monitoringSet.getPowerSystemResources());
			}
		});
		Assertions.assertEquals(1, receivedCount.get());
		Assertions.assertEquals(0, errorCount.get());
	}

	@Test
	void testMonitoringSetSubscription() throws Exception {

		//we will run the subscription for fixed number of requests
		AtomicInteger requestCounter = new AtomicInteger(0);
		String startTime = Instant.now().toString();
		String etag = UUID.randomUUID().toString();

		requestHandler = request -> {

			BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);

			try {

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
							new GzipCompressingEntity(new InputStreamEntity(in, ContentType.create(TrolieApiConstants.CONTENT_TYPE_MONITORING_SET))));
					response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
					threadPoolExecutor.submit((Callable<Void>) () -> {

                        try (JsonGenerator json = new JsonFactory(objectMapper).createGenerator(out)) {
                            writeMonitoringSet(json, startTime);
                            return null;
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
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
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri,builder.build())
				.monitoringSetPollMs(200).build();) {

			AtomicInteger monitoringSetsReceived = new AtomicInteger(0);
			AtomicInteger errorCount = new AtomicInteger(0);

			//subscribe for snapshots and validate they are transmitted correctly
			var subscription = trolieClient.subscribeToMonitoringSetUpdates(new MonitoringSetsSubscribedReceiver() {

				RequestSubscription subscription;

				@Override
				public void monitoringSet(MonitoringSet monitoringSet) {
					monitoringSetsReceived.incrementAndGet();
				}

				@Override
				public void error(StreamingGetException t) {
					errorCount.incrementAndGet();
					((RequestSubscriptionInternal)subscription).stop();
				}

				@Override
				public void setSubscription(RequestSubscription subscription) {
					this.subscription = subscription;
				}


			}, "abc");

			while (subscription.isSubscribed()) {
				Thread.sleep(100);
			}

			//we should have received 2 monitoring sets, 1 304 code and 1 500 code
			Assertions.assertEquals(2, monitoringSetsReceived.get());
			Assertions.assertEquals(1, errorCount.get());
		}
	}

	private void writeMonitoringSet(JsonGenerator json, String id) throws IOException {
		var source = DataProvenance.builder().provider(id).lastUpdated(
				Instant.now()).originId(id).build();
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

	private void writeForecastSnapshot(JsonGenerator json, Instant startTime) throws IOException {

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
						RatingValue.fromMva(100f),
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
					.continuousOperatingLimit(RatingValue.fromMva(100f)).build());
		}

		json.writeEndArray();
		json.writeEndObject();
	}
}
