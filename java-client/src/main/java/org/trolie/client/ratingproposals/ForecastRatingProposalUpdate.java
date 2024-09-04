package org.trolie.client.ratingproposals;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.core5.http.HttpHeaders;
import org.trolie.client.model.ratingproposals.ForecastProposalHeader;
import org.trolie.client.model.ratingproposals.ForecastRatingPeriod;
import org.trolie.client.model.ratingproposals.ForecastRatingProposalStatus;

import java.io.IOException;

public class ForecastRatingProposalUpdate implements AutoCloseable {

	private static final String PATH = "/rating-proposals/forecast";
	private static final String CONTENT_TYPE = "application/vnd.lep.rating-forecast-proposal.v1+json";

	private enum Scope {
		MAIN,
		RATING,
		PERIOD,
	}

	HttpClient httpClient;
	String baseUrl;

	JsonGenerator jsonGenerator;
	Scope scope = Scope.MAIN;
	HttpPatch httpPatch;

	public ForecastRatingProposalUpdate(HttpClient httpClient, String baseUrl) {
		super();
		this.httpClient = httpClient;
		this.baseUrl = baseUrl;
	}

	public void begin() throws IOException {
		validateScope(Scope.MAIN, Scope.MAIN);

		httpPatch = new HttpPatch(baseUrl + PATH);
		httpPatch.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE);

		// Need a thread pool executor with only one thread. 5 second time to live.

		// Primary thread -> builds up json body in output stream

		// Secondary thread -> executes request as buffer fills. Needs to implement callable to return a future that will
		// either return response or bubble up exception. Need to be able to interrupt thread with future.cancel()


		httpClient.execute(httpPatch, new BasicHttpClientResponseHandler());


		jsonGenerator = new JsonFactory().createGenerator();
		jsonGenerator.setCodec(new ObjectMapper());
		jsonGenerator.writeStartObject();

	}

	public void header(ForecastProposalHeader header) throws IOException {
		validateScope(Scope.MAIN, Scope.MAIN);

		jsonGenerator.writeFieldName("proposal-header");
		jsonGenerator.writeObject(header);
		jsonGenerator.writeArrayFieldStart("ratings");

	}

	public void beginResource(String resourceId) throws IOException {
		validateScope(Scope.RATING, Scope.MAIN);

		jsonGenerator.writeStartObject();
		jsonGenerator.writeFieldName("resource-id");
		jsonGenerator.writeString(resourceId);
		jsonGenerator.writeArrayFieldStart("periods");
	}

	public void period(ForecastRatingPeriod forecastRatingPeriod) throws IOException {

		//validate that we are not in main scope
		validateScope(Scope.PERIOD, Scope.RATING, Scope.PERIOD);

		jsonGenerator.writeObject(forecastRatingPeriod);
	}

	public void endResource() {
		// At least one period should've been submitted for the resource
		validateScope(Scope.MAIN, Scope.PERIOD);
	}

	public ForecastRatingProposalStatus complete() throws IOException {
		jsonGenerator.writeEndArray();
		jsonGenerator.writeEndObject();

		return null;
	}

	@Override
	public void close() throws Exception {

	}

	private void validateScope(Scope nextScope, Scope... acceptableScopes) {
		boolean acceptable = false;
		for (Scope acceptableScope : acceptableScopes) {
			if (scope.equals(acceptableScope)) {
				acceptable = true;
				break;
			}
		}

		if (!acceptable) {
			throw new IllegalStateException(String.format("Illegal transition from scope %s to %s", scope, nextScope));
		}

		scope = nextScope;
	}

}
