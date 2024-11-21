package org.trolie.client.request.ratingproposals;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trolie.client.TrolieException;
import org.trolie.client.model.ratingproposals.ForecastProposalHeader;
import org.trolie.client.model.ratingproposals.ForecastRatingPeriod;
import org.trolie.client.model.ratingproposals.ForecastRatingProposalStatus;
import org.trolie.client.request.streaming.AbstractStreamingUpdate;
import org.trolie.client.util.TrolieApiConstants;

import java.util.Map;
import java.util.function.Function;

public class ForecastRatingProposalUpdate extends AbstractStreamingUpdate<ForecastRatingProposalStatus> {

	private static final Logger logger = LoggerFactory.getLogger(ForecastRatingProposalUpdate.class);


	public ForecastRatingProposalUpdate(HttpClient httpClient, HttpHost host, RequestConfig requestConfig,
										int bufferSize, ObjectMapper objectMapper, Map<String, String> httpHeaders) {
		super(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders);
	}


	private enum Scope {
		BEGIN,
		MAIN,
		RATING,
		END
	}

	JsonGenerator jsonGenerator;
	Scope scope = Scope.BEGIN;

	@Override
	protected HttpUriRequestBase getRequest() {
		return new HttpPatch(getPath());
	}

	@Override
	protected ContentType getContentType() {
		return ContentType.create(TrolieApiConstants.CONTENT_TYPE_FORECAST_PROPOSAL);
	}

	@Override
	protected String getPath() {
		return TrolieApiConstants.PATH_FORECAST_PROPOSAL;
	}

	@Override
	protected Function<HttpEntity,ForecastRatingProposalStatus> getResponseHandler() {
		return e -> {
			try {
				return objectMapper.readValue(e.getContent(), ForecastRatingProposalStatus.class);
			} catch (Exception e2) {	
				throw new TrolieException("Failed to parse response",e2);
			}
		};
	}

	public void begin(ForecastProposalHeader header) {

		validateScope(Scope.MAIN, Scope.BEGIN);
		try {
			jsonGenerator = new JsonFactory().createGenerator(createRequestOutputStream());
		} catch (Exception e) {
			throw new TrolieException("Error creating request output stream",e);
		}
		
		checkCanWrite();

		try {	
			jsonGenerator.setCodec(objectMapper);
			jsonGenerator.writeStartObject();
			jsonGenerator.writeFieldName("proposal-header");
			jsonGenerator.writeObject(header);
			jsonGenerator.writeArrayFieldStart("ratings");
		} catch (Exception e) {
			handleWriteError(e);
		}

	}

	public void beginResource(String resourceId) {

		checkCanWrite();		
		try {
			validateScope(Scope.RATING, Scope.MAIN);
			jsonGenerator.writeStartObject();
			jsonGenerator.writeFieldName("resource-id");
			jsonGenerator.writeString(resourceId);
			jsonGenerator.writeArrayFieldStart("periods");
		} catch (Exception e) {
			handleWriteError(e);
		}
	}

	public void period(ForecastRatingPeriod forecastRatingPeriod) {
		checkCanWrite();
		try {
			validateScope(Scope.RATING, Scope.RATING);
			jsonGenerator.writeObject(forecastRatingPeriod);
		} catch (Exception e) {
			handleWriteError(e);
		}
	}

	public void endResource() {
		checkCanWrite();
		try {
			validateScope(Scope.MAIN, Scope.RATING);
			jsonGenerator.writeEndArray();
			jsonGenerator.writeEndObject();
		} catch (Exception e) {			
			handleWriteError(e);
		}
	}

	public ForecastRatingProposalStatus complete() {
		checkCanWrite();
		try {
			validateScope(Scope.END, Scope.MAIN);
			jsonGenerator.writeEndArray();
			jsonGenerator.writeEndObject();
			jsonGenerator.close();
		} catch (Exception e) {
			handleWriteError(e);
		}
		return completeRequest();
	}

	@Override
	public void close() {
		super.close();
		if (jsonGenerator != null && !jsonGenerator.isClosed()) {
			try {
				jsonGenerator.close();
			} catch (Exception e) {
				logger.debug("Error closing JSON generator", e);
			}
		}
	}

	private void validateScope(Scope nextScope, Scope... acceptableScopes) {

		boolean acceptable = false;

		for (Scope acceptableScope : acceptableScopes) {
			if (scope == acceptableScope) {
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
