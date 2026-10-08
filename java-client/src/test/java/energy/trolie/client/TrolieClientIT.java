package energy.trolie.client;

import energy.trolie.client.exception.StreamingGetException;
import energy.trolie.client.exception.TrolieException;
import energy.trolie.client.exception.TrolieServerException;
import energy.trolie.client.impl.request.RequestSubscriptionInternal;
import energy.trolie.client.model.common.DataProvenance;
import energy.trolie.client.model.common.EmergencyRatingValue;
import energy.trolie.client.model.common.PowerSystemResource;
import energy.trolie.client.model.common.RatingValue;
import energy.trolie.client.model.monitoringsets.MonitoringSet;
import energy.trolie.client.model.operatingsnapshots.ForecastPeriodSnapshot;
import energy.trolie.client.model.operatingsnapshots.ForecastSnapshotHeader;
import energy.trolie.client.model.operatingsnapshots.RealTimeLimit;
import energy.trolie.client.model.operatingsnapshots.RealTimeSnapshotHeader;
import energy.trolie.client.model.operatingsnapshots.SeasonalPeriodSnapshot;
import energy.trolie.client.model.operatingsnapshots.SeasonalSnapshotHeader;
import energy.trolie.client.model.ratingproposals.ForecastProposalHeader;
import energy.trolie.client.model.ratingproposals.ForecastRatingPeriod;
import energy.trolie.client.model.ratingproposals.ForecastRatingProposalStatus;
import energy.trolie.client.model.ratingproposals.ProposalHeader;
import energy.trolie.client.model.ratingproposals.RealTimeRating;
import energy.trolie.client.model.ratingproposals.RealTimeRatingProposalStatus;
import energy.trolie.client.model.temporaryaarexceptions.TemporaryAARException;
import energy.trolie.client.model.temporaryaarexceptions.TemporaryAARExceptionRequest;
import energy.trolie.client.request.monitoringsets.MonitoringSetsReceiver;
import energy.trolie.client.request.monitoringsets.MonitoringSetsSubscribedReceiver;
import energy.trolie.client.request.operatingsnapshots.ForecastSnapshotReceiver;
import energy.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscribedReceiver;
import energy.trolie.client.request.operatingsnapshots.RealTimeSnapshotReceiver;
import energy.trolie.client.request.operatingsnapshots.RealTimeSnapshotSubscribedReceiver;
import energy.trolie.client.request.operatingsnapshots.SeasonalSnapshotReceiver;
import energy.trolie.client.request.operatingsnapshots.SeasonalSnapshotSubscribedReceiver;
import energy.trolie.client.request.ratingproposals.ForecastRatingProposalUpdate;
import energy.trolie.client.request.ratingproposals.RealTimeRatingProposalUpdate;
import energy.trolie.client.spp.SppApiTokenHeaderProvider;
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
import org.apache.hc.core5.http.ProtocolException;
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
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.json.JsonMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
	static JsonMapper jsonMapper;

	ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1,1,1,TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());

	@BeforeAll
	public static void createTestServer() throws Exception {

		jsonMapper = new JsonMapper();

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

				Map<String,Object> data = jsonMapper.readValue(entity.getContent(),Map.class);
				List<Map<String,Object>> ratings = (List<Map<String,Object>>)data.get("ratings");
				Assertions.assertEquals(3, ratings.size());
				for (Map<String,Object> rating : ratings) {
					List<Map<String,Object>> periods = (List<Map<String,Object>>)rating.get("periods");
					Assertions.assertEquals(3, periods.size());
				}

				BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
				response.setEntity(new StringEntity(jsonMapper.writeValueAsString(status)));
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

						try (JsonGenerator json = jsonMapper.createGenerator(out)) {

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

							try (JsonGenerator json = jsonMapper.createGenerator(out)) {
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

                    try (JsonGenerator json = jsonMapper.createGenerator(out)) {

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

							try (JsonGenerator json = jsonMapper.createGenerator(out)) {
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

				Map<String,Object> data = jsonMapper.readValue(entity.getContent(),Map.class);
				List<Map<String,Object>> ratings = (List<Map<String,Object>>)data.get("ratings");
				Assertions.assertEquals(3, ratings.size());

				BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
				response.setEntity(new StringEntity(jsonMapper.writeValueAsString(status)));
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
							try (JsonGenerator json = jsonMapper.createGenerator(out)) {
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

						try (JsonGenerator json = jsonMapper.createGenerator(out)) {


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
                        try (JsonGenerator json = jsonMapper.createGenerator(out)) {
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

                    try (JsonGenerator json = jsonMapper.createGenerator(out)) {


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
                    try (JsonGenerator json = jsonMapper.createGenerator(out)) {
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
						try (JsonGenerator json = jsonMapper.createGenerator(out)) {
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

                        try (JsonGenerator json = jsonMapper.createGenerator(out)) {
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

	@Test
	void testSeasonalSnapshotGet() throws Exception {

		Instant startTime = Instant.now();
		String season = "FALL";

		requestHandler = request -> {

			BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);

			try {

				//we expect to get the monitoring set name as a query param
				Assertions.assertEquals(
						TrolieApiConstants.PARAM_MONITORING_SET + "=abc",
						request.getUri().getQuery());

				//on 1st and 3rd request, return a new snapshot to indicate an update
				PipedOutputStream out = new PipedOutputStream();
				PipedInputStream in = new PipedInputStream(out);

				response.setEntity(
						new GzipCompressingEntity(new InputStreamEntity(in,ContentType.create(TrolieApiConstants.CONTENT_TYPE_SEASONAL_SNAPSHOT))));
				response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
				threadPoolExecutor.submit(new Callable<Void>() {
					@Override
					public Void call() throws Exception {

						try (JsonGenerator json = jsonMapper.createGenerator(out)) {

							writeSeasonalSnapshot(json, startTime, season);

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
			trolieClient.getInUseSeasonalSnapshots(new SeasonalSnapshotReceiver() {

				int numResources;
				int numPeriods;

				@Override
				public void header(SeasonalSnapshotHeader header) {
					Assertions.assertNotNull(header);

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
				public void period(SeasonalPeriodSnapshot period) {

					Assertions.assertEquals( season, period.getSeasonName());
					numPeriods++;
				}


				@Override
				public void endResource() {
					Assertions.assertEquals(24, numPeriods);
					numPeriods = 0;
				}
			}, "abc", null);

			Assertions.assertEquals(1, snapshotsReceived.get());
			Assertions.assertEquals(0, errorCount.get());

		}
	}

	@Test
	void testSeasonalSnapshotSubscription() throws Exception {

		//we will run the subscription for fixed number of requests
		AtomicInteger requestCounter = new AtomicInteger(0);
		Instant startTime = Instant.now();
		String season = "FALL";
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
							new GzipCompressingEntity(new InputStreamEntity(in,ContentType.create(TrolieApiConstants.CONTENT_TYPE_SEASONAL_SNAPSHOT))));
					response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
					threadPoolExecutor.submit(new Callable<Void>() {
						@Override
						public Void call() throws Exception {

							try (JsonGenerator json = jsonMapper.createGenerator(out)) {
								writeSeasonalSnapshot(json, startTime, season );
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
				.seasonalRatingsPollMs(200)
				.build();) {

			AtomicInteger snapshotsReceived = new AtomicInteger(0);
			AtomicInteger errorCount = new AtomicInteger(0);

			//subscribe for snapshots and validate they are transmitted correctly
			var subscription = trolieClient.subscribeToInUseSeasonalSnapshotUpdates(new SeasonalSnapshotSubscribedReceiver() {

				RequestSubscription subscription;
				int numResources;
				int numPeriods;

				@Override
				public void period(SeasonalPeriodSnapshot period) {
					numPeriods++;
					Assertions.assertEquals(season, period.getSeasonName());

				}

				@Override
				public void header(SeasonalSnapshotHeader header) {

					Assertions.assertNotNull(header);
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
			int counter = 0;
			while (subscription.isSubscribed()) {
				Thread.sleep(100);
			}

			//we should have received 2 snapshots, 1 304 code and 1 500 code
			Assertions.assertEquals(2, snapshotsReceived.get());
			Assertions.assertEquals(1, errorCount.get());
		}
	}

	@Test
	void testDynamicRequestHeaderProvider() throws IOException {
		String expectedHeaderName = "X-TROLIE-Auth";
		String expectedHeaderValue = "dynamic-token-123";

		// Setup the request handler to verify the header exists
		requestHandler = request -> {
            try {
                Assertions.assertNotNull(request.getHeader(expectedHeaderName), "Header should be present");
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            }
            try {
                Assertions.assertEquals(expectedHeaderValue, request.getHeader(expectedHeaderName).getValue());
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            }

            BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);

			ForecastRatingProposalStatus status = ForecastRatingProposalStatus.builder().build();
			try {
				String jsonResponse = jsonMapper.writeValueAsString(status);
				response.setEntity(new StringEntity(jsonResponse, ContentType.APPLICATION_JSON));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			return response;
		};

		// Configure the client to use the new provider
		HttpClientBuilder builder = HttpClientBuilder.create();
		RequestHeaderProvider headerProvider = (context) -> Map.of(expectedHeaderName, expectedHeaderValue);

		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri + "/test-path", builder.build())
				.addRequestHeaderProvider( headerProvider)
				.build()) {

			//Perform a request that triggers the header provider
			try (ForecastRatingProposalUpdate update = trolieClient.createForecastRatingProposalStreamingUpdate()) {
				update.begin(ForecastProposalHeader.builder().begins(Instant.now()).build());
				update.complete();
			}
		}
	}

	@Test
	void testSppApiTokenHeaderProvider_generatesCorrectHeader() throws NoSuchAlgorithmException, InvalidKeyException {
		// Arrange
		String screenName = "TestScreenName";
		String rawApiKey = "my-secret-api-key-987654321";
		String base64ApiKey = Base64.getEncoder().encodeToString(rawApiKey.getBytes(StandardCharsets.UTF_8));
		Instant fixedInstant = Instant.parse("2026-06-18T12:34:56Z");
		Clock clock = Clock.fixed(fixedInstant, ZoneOffset.UTC);

		SppApiTokenHeaderProvider provider = new SppApiTokenHeaderProvider(screenName, base64ApiKey, clock);

		TrolieRequestContext context = new TrolieRequestContext(
				"GET",
				URI.create("https://trolie.example.com/api/v1/ratings?query=abc"),
				"application/json"
		);

		// Act
		Map<String, String> headers = provider.headersFor(context);

		// Assert
		assertNotNull(headers);
		assertTrue(headers.containsKey("X-SPP-API-Token"));

		String token = headers.get("X-SPP-API-Token");
		assertNotNull(token);

		// Token format should be: timestamp-nonce-hmacHash
		// Timestamp is "2026-06-18T12:34:56Z" (length 20)
		String expectedTimestamp = "2026-06-18T12:34:56Z";
		assertTrue(token.startsWith(expectedTimestamp + "-"), "Token should start with the expected timestamp followed by a hyphen");

		// Extract materials following the prefix
		String remainder = token.substring(expectedTimestamp.length() + 1);

		// A UUID has 36 characters (e.g., 8-4-4-4-12)
		assertTrue(remainder.length() > 36, "Token remainder must be longer than UUID length");
		String nonceStr = remainder.substring(0, 36);
		// Verify it is a valid UUID
		assertDoesNotThrow(() -> UUID.fromString(nonceStr), "Nonce should be a valid UUID");

		assertEquals('-', remainder.charAt(36), "Character after UUID should be a hyphen");
		String hmacHash = remainder.substring(37);

		// Verify HMAC authenticity
		String lowerScreenName = "testscreenname";
		String lowerPath = "/api/v1/ratings";
		String expectedStringToSign = nonceStr + expectedTimestamp + lowerScreenName + lowerPath;

		byte[] expectedHmacBytes;
		Mac mac = Mac.getInstance("HmacSHA512");
		SecretKeySpec secretKeySpec = new SecretKeySpec(rawApiKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
		mac.init(secretKeySpec);
		expectedHmacBytes = mac.doFinal(expectedStringToSign.getBytes(StandardCharsets.UTF_8));
		String expectedHmacHash = Base64.getEncoder().encodeToString(expectedHmacBytes);

		assertEquals(expectedHmacHash, hmacHash, "HMAC signature should match the expected signature over metadata");
	}

	private void writeMonitoringSet(JsonGenerator json, String id) throws IOException {
		var source = DataProvenance.builder().provider(id).lastUpdated(
				Instant.now()).originId(id).build();
		MonitoringSet monitoringSet = new MonitoringSet("monitoringSetName", "This is test SDK", List.of(), source, id);
		json.writeStartObject();
		try {
			json.writeName(TAG_SOURCE);
			jsonMapper.writeValue(json, monitoringSet.getSource());
			json.writeName(TAG_ID);
			jsonMapper.writeValue(json, monitoringSet.getId());
			json.writeName(TAG_DESCRIPTION);
			jsonMapper.writeValue(json, monitoringSet.getDescription());
			json.writeName(TAG_POWER_SYSTEM_RESOURCES);
			jsonMapper.writeValue(json, monitoringSet.getPowerSystemResources());
		}catch (Exception e) {
			log.error("writeMonitoringSet.error ", e);
		}
		json.writeEndObject();
	}

	private void writeForecastSnapshot(JsonGenerator json, Instant startTime) throws IOException {

		ForecastSnapshotHeader header = new ForecastSnapshotHeader(startTime);

		json.writeStartObject();

		json.writeName("snapshot-header");
		jsonMapper.writeValue(json, header);

		json.writeName("ratings");
		json.writeStartArray();

		for (int i=0;i<100;i++) {
			json.writeStartObject();
			json.writeName("resource-id");
			json.writeString("resource" + i);
			json.writeName("periods");
			json.writeStartArray();
			for (int j=0;j<24;j++) {
				ForecastPeriodSnapshot period = ForecastPeriodSnapshot.builder()
						.periodStart(startTime)
						.periodEnd(startTime)
						.continuousOperatingLimit(RatingValue.fromMva(100f))
						.emergencyOperatingLimits(Collections.emptyList())
						.build();
				jsonMapper.writeValue(json, period);
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
		json.writeName("snapshot-header");
		jsonMapper.writeValue(json, header);

		json.writeName("ratings");
		json.writeStartArray();

		for (int i=0;i<100;i++) {
			jsonMapper.writeValue(json, RealTimeLimit.builder()
					.resourceId("resource" + i)
					.continuousOperatingLimit(RatingValue.fromMva(100f)).build());
		}

		json.writeEndArray();
		json.writeEndObject();
	}

	private void writeSeasonalSnapshot(JsonGenerator json, Instant startTime, String season) throws IOException {

		SeasonalSnapshotHeader header = new SeasonalSnapshotHeader();

		json.writeStartObject();

		json.writeName("snapshot-header");
		jsonMapper.writeValue(json, header);

		json.writeName("ratings");
		json.writeStartArray();

		for (int i=0;i<100;i++) {
			json.writeStartObject();
			json.writeName("resource-id");
			json.writeString("resource" + i);
			json.writeName("periods");
			json.writeStartArray();
			for (int j=0;j<24;j++) {
				SeasonalPeriodSnapshot period = SeasonalPeriodSnapshot.builder()
						.periodStart(startTime)
						.periodEnd(startTime)
						.seasonName(season)
						.continuousOperatingLimit(RatingValue.fromMva(100f))
						.emergencyOperatingLimits(Collections.emptyList())
						.build();
				jsonMapper.writeValue(json, period);
			}
			json.writeEndArray();
			json.writeEndObject();
		}

		json.writeEndArray();
		json.writeEndObject();

	}

	@Test
	void testRequestHeaderProviderOnParameterizedGet() throws Exception {
		// Verify that the RequestHeaderProvider is invoked on the GET path,
		// that context.uri().getQuery() is non-null and contains the expected
		// query parameters, and that context.contentType() is null for GETs.

		Instant startTime = Instant.now();
		String startTimeString = startTime.toString();

		requestHandler = request -> {
			BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
			try {
				PipedOutputStream out = new PipedOutputStream();
				PipedInputStream in = new PipedInputStream(out);
				response.setEntity(new GzipCompressingEntity(new InputStreamEntity(in,
						ContentType.create(TrolieApiConstants.CONTENT_TYPE_FORECAST_SNAPSHOT))));
				response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
				threadPoolExecutor.submit((Callable<Void>) () -> {
					try (JsonGenerator json = jsonMapper.createGenerator(out)) {
						writeForecastSnapshot(json, startTime);
						return null;
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}
			return response;
		};

		AtomicInteger providerInvocationCount = new AtomicInteger(0);
		// Captured context fields
		String[] capturedQuery = {null};
		String[] capturedMethod = {null};
		String[] capturedContentType = {"NOT_SET"};

		RequestHeaderProvider provider = context -> {
			providerInvocationCount.incrementAndGet();
			capturedMethod[0] = context.method();
			capturedQuery[0] = context.uri().getQuery();
			capturedContentType[0] = context.contentType();
			return Map.of();
		};

		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri, builder.build())
				.addRequestHeaderProvider(provider)
				.build()) {

			trolieClient.getInUseLimitForecasts(new ForecastSnapshotReceiver() {
				@Override public void header(ForecastSnapshotHeader header) {}
				@Override public void beginSnapshot() {}
				@Override public void endSnapshot() {}
				@Override public void beginResource(String resourceId) {}
				@Override public void period(ForecastPeriodSnapshot period) {}
				@Override public void endResource() {}
            }, "abc", null, startTime, startTime);
		}

		// Provider must have been called exactly once for the single GET
		Assertions.assertEquals(1, providerInvocationCount.get(),
				"Provider should be invoked once for the GET request");

		// Method must be GET
		Assertions.assertEquals("GET", capturedMethod[0]);

		// Query must be non-null and contain the expected parameters
		Assertions.assertNotNull(capturedQuery[0],
				"context.uri().getQuery() must not be null for a parameterized GET");
		Assertions.assertTrue(capturedQuery[0].contains(TrolieApiConstants.PARAM_MONITORING_SET + "=abc"),
				"Query should contain monitoring-set param");
		Assertions.assertTrue(capturedQuery[0].contains(TrolieApiConstants.PARAM_OFFSET_PERIOD_START + "=" + startTimeString),
				"Query should contain offset-period-start param");
		Assertions.assertTrue(capturedQuery[0].contains(TrolieApiConstants.PARAM_PERIOD_END + "=" + startTimeString),
				"Query should contain period-end param");

		// contentType must be null for a GET (no request body)
		Assertions.assertNull(capturedContentType[0],
				"context.contentType() must be null for GET requests");
	}

	@Test
	void testMultipleProvidersAndStaticHeaderPrecedence() throws IOException {
		// Validates the documented merge order:
		//   static httpHeaders  →  provider1  →  provider2  (later values overwrite earlier ones)
		//
		// Setup:
		//   X-Static   set by static headers ("static-value")
		//              then overwritten by provider1 ("provider1-value")
		//   X-P1-Only  set only by provider1 ("p1-only")
		//              then overwritten by provider2 ("provider2-wins")
		//   X-P2-Only  set only by provider2 ("p2-only")
		//
		// Expected on the server:
		//   X-Static   = "provider1-value"   (provider1 overwrites static)
		//   X-P1-Only  = "provider2-wins"    (provider2 overwrites provider1)
		//   X-P2-Only  = "p2-only"           (only provider2 touches this key)

		requestHandler = request -> {
			try {
				Assertions.assertEquals("provider1-value",
						request.getHeader("X-Static").getValue(),
						"provider1 must overwrite the static header value");
				Assertions.assertEquals("provider2-wins",
						request.getHeader("X-P1-Only").getValue(),
						"provider2 must overwrite provider1 for the same key");
				Assertions.assertEquals("p2-only",
						request.getHeader("X-P2-Only").getValue(),
						"provider2-only header must be present");
			} catch (ProtocolException e) {
				throw new RuntimeException(e);
			}

			BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
			try {
				response.setEntity(new StringEntity(
						jsonMapper.writeValueAsString(ForecastRatingProposalStatus.builder().build()),
						ContentType.APPLICATION_JSON));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return response;
		};

		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri + "/test-path", builder.build())
				.httpHeaders(Map.of("X-Static", "static-value"))
				.addRequestHeaderProvider(ctx -> Map.of(
						"X-Static",  "provider1-value",   // overwrites static
						"X-P1-Only", "p1-only"))
				.addRequestHeaderProvider(ctx -> Map.of(
						"X-P1-Only", "provider2-wins",    // overwrites provider1
						"X-P2-Only", "p2-only"))
				.build()) {

			try (ForecastRatingProposalUpdate update = trolieClient.createForecastRatingProposalStreamingUpdate()) {
				update.begin(ForecastProposalHeader.builder().begins(Instant.now()).build());
				update.complete();
			}
		}
	}

	@Test
	void testProviderReInvocationOnSubscribedGetPolls() throws Exception {
		// Test that verifies providers are re-called on repeated poll cycles.
		// This validates that for subscribed-GET requests, the RequestHeaderProvider
		// is invoked fresh on each poll cycle, ensuring dynamic content (e.g., auth tokens, nonces)
		// is regenerated for every request.

		AtomicInteger requestCounter = new AtomicInteger(0);
		AtomicInteger providerInvocationCount = new AtomicInteger(0);
		String etag = UUID.randomUUID().toString();
		Instant startTime = Instant.now();

		requestHandler = request -> {
			BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);

			try {
				// Verify that the request has the expected header from the provider
				Header dynamicHeader = request.getHeader("X-Provider-Test");
				Assertions.assertNotNull(dynamicHeader, "Provider header should be present on every request");
				Assertions.assertEquals("provider-invocation-" + requestCounter.get(), dynamicHeader.getValue());

				if (requestCounter.get() == 0) {
					// First poll: return forecast snapshot with etag
					PipedOutputStream out = new PipedOutputStream();
					PipedInputStream in = new PipedInputStream(out);

					response.setHeader(HttpHeaders.ETAG, etag);
					response.setEntity(
							new GzipCompressingEntity(new InputStreamEntity(in, ContentType.create(TrolieApiConstants.CONTENT_TYPE_FORECAST_SNAPSHOT))));
					response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
					threadPoolExecutor.submit(new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							try (JsonGenerator json = jsonMapper.createGenerator(out)) {
								writeForecastSnapshot(json, startTime);
								return null;
							} catch (Exception e) {
								e.printStackTrace();
								throw new RuntimeException(e);
							}
						}
					});
				} else if (requestCounter.get() == 1) {
					// Second poll: return 304 Not Modified (provider should still be called)
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
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri, builder.build())
				.forecastRatingsPollMs(200)
				.addRequestHeaderProvider(context -> {
					// Increment provider invocation counter
					int invocationNumber = providerInvocationCount.getAndIncrement();
					log.info("Provider invoked for poll cycle {}", invocationNumber);
					// Return headers that include the invocation number for verification
					return Map.of("X-Provider-Test", "provider-invocation-" + requestCounter.get());
				})
				.build()) {

			AtomicInteger snapshotsReceived = new AtomicInteger(0);
			AtomicInteger errorCount = new AtomicInteger(0);

			// Subscribe and run for two poll cycles
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

			// Wait for two poll cycles to complete
			// First request sends data, second request gets 304 Not Modified
			int waitCycles = 0;
			while (subscription.isSubscribed() && waitCycles < 20) {
				Thread.sleep(100);
				waitCycles++;
				if (requestCounter.get() >= 2) {
					((RequestSubscriptionInternal) subscription).stop();
					break;
				}
			}

			// Verify the provider was invoked on each poll cycle
			Assertions.assertEquals(2, providerInvocationCount.get(),
					"Provider should be invoked exactly twice (once per poll cycle)");
			Assertions.assertEquals(1, snapshotsReceived.get(),
					"Should have received one snapshot on first poll");
			Assertions.assertEquals(0, errorCount.get(),
					"Should have no errors during subscription");
		}
	}

	@Test
	void testCreateTemporaryAARException() throws IOException {

		var startTime = Instant.now();

		requestHandler = request -> {
			try {
				Assertions.assertEquals("POST", request.getMethod());
				Assertions.assertEquals("/temporary-aar-exceptions", request.getUri().getPath());

				var sentRequest = jsonMapper.readValue(request.getEntity().getContent(), TemporaryAARExceptionRequest.class);
				Assertions.assertEquals("8badf00d", sentRequest.getResource().getResourceId());
				Assertions.assertNull(sentRequest.getSource());

				TemporaryAARException created = TemporaryAARException.builder()
						.id("46f7212b-1633-4c30-ba71-c6e987b2ded7")
						.resource(sentRequest.getResource())
						.startTime(sentRequest.getStartTime())
						.endTime(sentRequest.getEndTime())
						.continuousOperatingLimit(sentRequest.getContinuousOperatingLimit())
						.emergencyOperatingLimits(sentRequest.getEmergencyOperatingLimits())
						.reason(sentRequest.getReason())
						.build();

				BasicClassicHttpResponse response = new BasicClassicHttpResponse(201);
				response.setEntity(new StringEntity(jsonMapper.writeValueAsString(created),
						ContentType.create(TrolieApiConstants.CONTENT_TYPE_TEMPORARY_AAR_EXCEPTION)));
				return response;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri, builder.build()).build()) {

			TemporaryAARExceptionRequest request = TemporaryAARExceptionRequest.builder()
					.resource(PowerSystemResource.of("8badf00d", List.of()))
					.startTime(startTime)
					.endTime(startTime.plusSeconds(3600))
					.continuousOperatingLimit(RatingValue.fromMva(160f))
					.emergencyOperatingLimits(List.of(EmergencyRatingValue.of("emergency", RatingValue.fromMva(165f))))
					.reason("High wildfire risk")
					.build();

			TemporaryAARException created = trolieClient.createTemporaryAARException(request);

			Assertions.assertNotNull(created);
			Assertions.assertEquals("46f7212b-1633-4c30-ba71-c6e987b2ded7", created.getId());
			Assertions.assertEquals("8badf00d", created.getResource().getResourceId());
		}
	}

	@Test
	void testCreateTemporaryAARException_withSourceForPeerReplication() throws IOException {

		// verifies that the `source` (data-provenance) field used to track a
		// Temporary AAR Exception's origin across peered systems round-trips correctly.

		var startTime = Instant.now();

		requestHandler = request -> {
			try {
				var sentRequest = jsonMapper.readValue(request.getEntity().getContent(), TemporaryAARExceptionRequest.class);
				Assertions.assertNotNull(sentRequest.getSource());
				Assertions.assertEquals("TO1", sentRequest.getSource().getProvider());
				Assertions.assertEquals("origin-123", sentRequest.getSource().getOriginId());

				TemporaryAARException created = TemporaryAARException.builder()
						.id("iso-assigned-id")
						.source(sentRequest.getSource())
						.resource(sentRequest.getResource())
						.startTime(sentRequest.getStartTime())
						.continuousOperatingLimit(sentRequest.getContinuousOperatingLimit())
						.emergencyOperatingLimits(sentRequest.getEmergencyOperatingLimits())
						.build();

				BasicClassicHttpResponse response = new BasicClassicHttpResponse(201);
				response.setEntity(new StringEntity(jsonMapper.writeValueAsString(created),
						ContentType.create(TrolieApiConstants.CONTENT_TYPE_TEMPORARY_AAR_EXCEPTION)));
				return response;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri, builder.build()).build()) {

			TemporaryAARExceptionRequest request = TemporaryAARExceptionRequest.builder()
					.source(DataProvenance.builder()
							.provider("TO1")
							.originId("origin-123")
							.lastUpdated(startTime)
							.build())
					.resource(PowerSystemResource.of("8badf00d", List.of()))
					.startTime(startTime)
					.continuousOperatingLimit(RatingValue.fromMva(160f))
					.emergencyOperatingLimits(List.of(EmergencyRatingValue.of("emergency", RatingValue.fromMva(165f))))
					.build();

			TemporaryAARException created = trolieClient.createTemporaryAARException(request);

			Assertions.assertEquals("TO1", created.getSource().getProvider());
			Assertions.assertEquals("origin-123", created.getSource().getOriginId());
		}
	}

	@Test
	void testGetTemporaryAARException() throws IOException {

		requestHandler = request -> {
			try {
				Assertions.assertEquals("GET", request.getMethod());
				Assertions.assertEquals("/temporary-aar-exceptions/46f7212b-1633-4c30-ba71-c6e987b2ded7",
						request.getUri().getPath());

				TemporaryAARException found = TemporaryAARException.builder()
						.id("46f7212b-1633-4c30-ba71-c6e987b2ded7")
						.resource(PowerSystemResource.of("8badf00d", List.of()))
						.startTime(Instant.now())
						.continuousOperatingLimit(RatingValue.fromMva(160f))
						.emergencyOperatingLimits(List.of(EmergencyRatingValue.of("emergency", RatingValue.fromMva(165f))))
						.build();

				BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
				response.setEntity(new StringEntity(jsonMapper.writeValueAsString(found),
						ContentType.create(TrolieApiConstants.CONTENT_TYPE_TEMPORARY_AAR_EXCEPTION)));
				return response;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri, builder.build()).build()) {
			TemporaryAARException found = trolieClient.getTemporaryAARException("46f7212b-1633-4c30-ba71-c6e987b2ded7");
			Assertions.assertNotNull(found);
			Assertions.assertEquals("46f7212b-1633-4c30-ba71-c6e987b2ded7", found.getId());
		}
	}

	@Test
	void testGetTemporaryAARException_notFound() throws IOException {

		requestHandler = request -> new BasicClassicHttpResponse(404);

		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri, builder.build()).build()) {
			TrolieServerException ex = Assertions.assertThrows(TrolieServerException.class,
					() -> trolieClient.getTemporaryAARException("does-not-exist"));
			Assertions.assertEquals(404, ex.getHttpCode());
		}
	}

	@Test
	void testGetTemporaryAARExceptions_withFilters() throws IOException {

		var startTime = Instant.now();

		requestHandler = request -> {
			try {
				Assertions.assertEquals("GET", request.getMethod());
				Assertions.assertEquals("/temporary-aar-exceptions", request.getUri().getPath());

				String query = request.getUri().getQuery();
				Assertions.assertTrue(query.contains("segment=segmentX"));
				Assertions.assertTrue(query.contains("monitoring-set=X-AMPL"));

				List<TemporaryAARException> exceptions = List.of(
						TemporaryAARException.builder()
								.id("id-1")
								.resource(PowerSystemResource.of("resource-1", List.of()))
								.startTime(startTime)
								.continuousOperatingLimit(RatingValue.fromMva(160f))
								.emergencyOperatingLimits(List.of(EmergencyRatingValue.of("emergency", RatingValue.fromMva(165f))))
								.build(),
						TemporaryAARException.builder()
								.id("id-2")
								.resource(PowerSystemResource.of("resource-2", List.of()))
								.startTime(startTime)
								.continuousOperatingLimit(RatingValue.fromMva(160f))
								.emergencyOperatingLimits(List.of(EmergencyRatingValue.of("emergency", RatingValue.fromMva(165f))))
								.build()
				);

				BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
				response.setEntity(new StringEntity(jsonMapper.writeValueAsString(exceptions),
						ContentType.create(TrolieApiConstants.CONTENT_TYPE_TEMPORARY_AAR_EXCEPTION_SET)));
				return response;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri, builder.build()).build()) {
			List<TemporaryAARException> exceptions = trolieClient.getTemporaryAARExceptions(
					startTime, startTime.plusSeconds(3600), "segmentX", "X-AMPL");

			Assertions.assertEquals(2, exceptions.size());
			Assertions.assertEquals("id-1", exceptions.get(0).getId());
			Assertions.assertEquals("id-2", exceptions.get(1).getId());
		}
	}

	@Test
	void testGetTemporaryAARExceptions_noFilters() throws IOException {

		requestHandler = request -> {
			try {
				Assertions.assertEquals("/temporary-aar-exceptions", request.getUri().getPath());
				Assertions.assertNull(request.getUri().getQuery());

				BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
				response.setEntity(new StringEntity(jsonMapper.writeValueAsString(List.of()),
						ContentType.create(TrolieApiConstants.CONTENT_TYPE_TEMPORARY_AAR_EXCEPTION_SET)));
				return response;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri, builder.build()).build()) {
			List<TemporaryAARException> exceptions = trolieClient.getTemporaryAARExceptions();
			Assertions.assertTrue(exceptions.isEmpty());
		}
	}

	@Test
	void testUpdateTemporaryAARException() throws IOException {

		// also covers termination, which TROLIE represents as an update with an earlier end-time

		var startTime = Instant.now();
		var newEndTime = startTime.plusSeconds(600);

		requestHandler = request -> {
			try {
				Assertions.assertEquals("PUT", request.getMethod());
				Assertions.assertEquals("/temporary-aar-exceptions/46f7212b-1633-4c30-ba71-c6e987b2ded7",
						request.getUri().getPath());

				var sentRequest = jsonMapper.readValue(request.getEntity().getContent(), TemporaryAARExceptionRequest.class);
				Assertions.assertEquals(newEndTime, sentRequest.getEndTime());

				return new BasicClassicHttpResponse(204);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri, builder.build()).build()) {

			TemporaryAARExceptionRequest request = TemporaryAARExceptionRequest.builder()
					.resource(PowerSystemResource.of("8badf00d", List.of()))
					.startTime(startTime)
					.endTime(newEndTime)
					.continuousOperatingLimit(RatingValue.fromMva(160f))
					.emergencyOperatingLimits(List.of(EmergencyRatingValue.of("emergency", RatingValue.fromMva(165f))))
					.build();

			Assertions.assertDoesNotThrow(
					() -> trolieClient.updateTemporaryAARException("46f7212b-1633-4c30-ba71-c6e987b2ded7", request));
		}
	}

	@Test
	void testUpdateTemporaryAARException_ServerError() throws IOException {

		requestHandler = request -> new BasicClassicHttpResponse(409);

		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri, builder.build()).build()) {

			TemporaryAARExceptionRequest request = TemporaryAARExceptionRequest.builder()
					.resource(PowerSystemResource.of("8badf00d", List.of()))
					.startTime(Instant.now())
					.continuousOperatingLimit(RatingValue.fromMva(160f))
					.emergencyOperatingLimits(List.of(EmergencyRatingValue.of("emergency", RatingValue.fromMva(165f))))
					.build();

			TrolieServerException ex = Assertions.assertThrows(TrolieServerException.class,
					() -> trolieClient.updateTemporaryAARException("some-id", request));
			Assertions.assertEquals(409, ex.getHttpCode());
		}
	}

	@Test
	void testDeleteTemporaryAARException() throws IOException {

		requestHandler = request -> {
			try {
				Assertions.assertEquals("DELETE", request.getMethod());
				Assertions.assertEquals("/temporary-aar-exceptions/46f7212b-1633-4c30-ba71-c6e987b2ded7",
						request.getUri().getPath());
				return new BasicClassicHttpResponse(204);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri, builder.build()).build()) {
			Assertions.assertDoesNotThrow(
					() -> trolieClient.deleteTemporaryAARException("46f7212b-1633-4c30-ba71-c6e987b2ded7"));
		}
	}

	@Test
	void testDeleteTemporaryAARException_conflict() throws IOException {

		// e.g. TROLIE 409: Temporary AAR Exception already employed in Operations cannot be deleted.
		requestHandler = request -> new BasicClassicHttpResponse(409);

		HttpClientBuilder builder = HttpClientBuilder.create();
		try (TrolieClient trolieClient = new TrolieClientBuilder(baseUri, builder.build()).build()) {
			TrolieServerException ex = Assertions.assertThrows(TrolieServerException.class,
					() -> trolieClient.deleteTemporaryAARException("in-use-id"));
			Assertions.assertEquals(409, ex.getHttpCode());
		}
	}

}
