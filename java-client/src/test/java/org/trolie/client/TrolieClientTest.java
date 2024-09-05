package org.trolie.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.net.ServerSocketFactory;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.io.DefaultBHttpServerConnectionFactory;
import org.apache.hc.core5.http.impl.io.HttpService;
import org.apache.hc.core5.http.io.HttpServerRequestHandler;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.protocol.DefaultHttpProcessor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.trolie.client.model.ratingproposals.ForecastProposalHeader;
import org.trolie.client.model.ratingproposals.ForecastRatingPeriod;
import org.trolie.client.model.ratingproposals.ForecastRatingProposalStatus;
import org.trolie.client.request.ratingproposals.ForecastRatingProposalStreamingUpdate;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TrolieClientTest {

	private static String HOST = "http://localhost:8080";

	static HttpServer httpServer;
	static Function<ClassicHttpRequest,ClassicHttpResponse> requestHandler;
	static ObjectMapper objectMapper;

	@BeforeAll
	public static void createTestServer() throws Exception {

		objectMapper = new ObjectMapper();

		//create a simple HTTP server we can send requests to
		
		httpServer = new HttpServer(
				8080, 
				HttpService.builder()
				.withHttpProcessor(new DefaultHttpProcessor(
						new HttpRequestInterceptor[0],
						new HttpResponseInterceptor[0]
						))
				.withHttpServerRequestHandler(new TestServerRequestHandler())
				.build(),
				InetAddress.getByName("127.0.0.1"), 
				SocketConfig.DEFAULT, 
				ServerSocketFactory.getDefault(), 
				DefaultBHttpServerConnectionFactory.builder().build(), 
				null, 
				null);

		httpServer.start();
	}

	@AfterAll
	public static void cleanup() {
		if (httpServer != null) {
			httpServer.initiateShutdown();
		}
	}

	@Test
	public void testForecastRatingProposalStreamingUpdate() {

		//test a roundtrip submission and response 
		
		String startTime = Instant.now().toString();

		requestHandler = request -> {

			ForecastRatingProposalStatus status = ForecastRatingProposalStatus.builder()
					.begins(startTime)
					.build();

			try {

				Map<String,Object> data = objectMapper.readValue(request.getEntity().getContent(),Map.class);
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
		TrolieClient trolieClient = new TrolieClientBuilder(HOST,builder).build();

		try (ForecastRatingProposalStreamingUpdate update = trolieClient.createForecastRatingProposalStreamingUpdate()) {

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

	@Test
	public void testForecastRatingProposalStreamingUpdate_ServerError() {

		//make sure that server errors are clearly bubbled up with a status code
		
		String startTime = Instant.now().toString();

		requestHandler = request -> {
			BasicClassicHttpResponse response = new BasicClassicHttpResponse(500);
			return response;
		};

		HttpClientBuilder builder = HttpClientBuilder.create();
		TrolieClient trolieClient = new TrolieClientBuilder(HOST,builder).build();

		Assertions.assertThrows(TrolieServerException.class, () -> {
			try (ForecastRatingProposalStreamingUpdate update = trolieClient.createForecastRatingProposalStreamingUpdate()) {

				ForecastProposalHeader header = ForecastProposalHeader.builder()
						.begins(startTime)
						.build();

				update.begin(header);
				update.complete();
			}
		});
	}

	@Test
	public void testForecastRatingProposalStreamingUpdate_ConnectError() {

		//make sure that client I/O errors are clearly bubbled up
		
		String startTime = Instant.now().toString();

		HttpClientBuilder builder = HttpClientBuilder.create();
		TrolieClient trolieClient = new TrolieClientBuilder("fakehost.com",builder).build();

			try (ForecastRatingProposalStreamingUpdate update = trolieClient.createForecastRatingProposalStreamingUpdate()) {

				ForecastProposalHeader header = ForecastProposalHeader.builder()
						.begins(startTime)
						.build();

				//must create enough data to fill the buffer for this to be a good test so 
				//we can make sure stream is not jammed up by death of request execution thread
				//and no bytes being taken off the buffer
				update.begin(header);
				for (int i=0;i<1000;i++) {
					update.beginResource("resource" + i);
					for (int j=0;j<100;j++) {
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
				Assertions.assertEquals(UnknownHostException.class, e.getCause().getClass());
			}
	}
	
	private static class TestServerRequestHandler implements HttpServerRequestHandler {

		@Override
		public void handle(
				ClassicHttpRequest request, 
				ResponseTrigger responseTrigger, 
				HttpContext context)
						throws HttpException, IOException {

			responseTrigger.submitResponse(requestHandler.apply(request));
		}

	}

}
