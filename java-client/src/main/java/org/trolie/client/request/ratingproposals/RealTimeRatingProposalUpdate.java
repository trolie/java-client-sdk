package org.trolie.client.request.ratingproposals;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trolie.client.TrolieException;
import org.trolie.client.model.ratingproposals.ProposalHeader;
import org.trolie.client.model.ratingproposals.RealTimeRating;
import org.trolie.client.model.ratingproposals.RealTimeRatingProposalStatus;
import org.trolie.client.request.streaming.AbstractStreamingUpdate;
import org.trolie.client.util.TrolieApiConstants;

import java.util.Map;
import java.util.function.Function;

public class RealTimeRatingProposalUpdate extends AbstractStreamingUpdate<RealTimeRatingProposalStatus> {

	private static final Logger logger = LoggerFactory.getLogger(RealTimeRatingProposalUpdate.class);

	public RealTimeRatingProposalUpdate(HttpClient httpClient, HttpHost host, RequestConfig requestConfig,
										int bufferSize, ObjectMapper objectMapper, Map<String, String> httpHeader) {
		super(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeader);
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
		//only need to establish the HTTP method. headers/params are handled by base class 
		return new HttpPost(getPath());
	}

	@Override
	protected ContentType getContentType() {
		return ContentType.create(TrolieApiConstants.CONTENT_TYPE_REALTIME_PROPOSAL);
	}

	@Override
	protected String getPath() {
		return TrolieApiConstants.PATH_REALTIME_PROPOSAL;
	}

	@Override
	protected Function<HttpEntity,RealTimeRatingProposalStatus> getResponseHandler() {
		return e -> {
			try {
				return objectMapper.readValue(e.getContent(), RealTimeRatingProposalStatus.class);
			} catch (Exception e2) {	
				throw new TrolieException("Failed to parse response",e2);
			}
		};
	}

	public void begin(ProposalHeader header) {

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

	public void rating(RealTimeRating rating) {
		checkCanWrite();
		try {
			validateScope(Scope.RATING, Scope.RATING, Scope.MAIN);
			jsonGenerator.writeObject(rating);
		} catch (Exception e) {
			handleWriteError(e);
		}
	}

	public RealTimeRatingProposalStatus complete() {
		checkCanWrite();
		try {
			validateScope(Scope.END, Scope.MAIN, Scope.RATING);
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
