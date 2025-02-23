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
import org.trolie.client.exception.TrolieException;
import org.trolie.client.impl.model.ratingproposals.ForecastPeriodBuilderImpl;
import org.trolie.client.model.ratingproposals.ForecastPeriodBuilder;
import org.trolie.client.model.ratingproposals.ForecastProposalHeader;
import org.trolie.client.model.ratingproposals.ForecastRatingPeriod;
import org.trolie.client.model.ratingproposals.ForecastRatingProposalStatus;
import org.trolie.client.impl.request.AbstractStreamingUpdate;
import org.trolie.client.TrolieApiConstants;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

/**
 * <p>Streaming update for forecast rating proposals.  Users stream out proposals by invoking methods
 * in the following sequence:</p>
 * <ol>
 *     <li>{@link #begin(ForecastProposalHeader)} with a completely populated header.</li>
 *     <li>{@link #beginResource(String)} for each resource.  Then, within that resource,</li>
 *     <li>{@link #periodBuilder()}  for each period in the forecast window.</li>
 *     <li>{@link #endResource()} before calling {@link #beginResource(String)} again for a new resource.</li>
 *     <li>{@link #complete()} to synchronously finish the request.</li>
 * </ol>
 */
public class ForecastRatingProposalUpdate extends AbstractStreamingUpdate<ForecastRatingProposalStatus> {

	private static final Logger logger = LoggerFactory.getLogger(ForecastRatingProposalUpdate.class);


	/**
	 * Constructs a new update.  Should not be called by application code.
	 * @param httpClient client
	 * @param host host
	 * @param requestConfig HTTP client request config
	 * @param bufferSize configure buffer size
	 * @param objectMapper Jackson object mapper
	 * @param httpHeaders passed headers
	 * @param defaultIntervalMinutes forecast interval minutes
	 */
	public ForecastRatingProposalUpdate(HttpClient httpClient, HttpHost host, RequestConfig requestConfig,
										int bufferSize, ObjectMapper objectMapper, Map<String, String> httpHeaders,
										int defaultIntervalMinutes) {
		super(httpClient, host, requestConfig, bufferSize, objectMapper, httpHeaders);
		this.defaultIntervalMinutes = defaultIntervalMinutes;
	}

    private Instant windowBegin;
	private final int defaultIntervalMinutes;


	private enum Scope {
		BEGIN,
		MAIN,
		RATING,
		END
	}

	private JsonGenerator jsonGenerator;
	private Scope scope = Scope.BEGIN;

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

	/**
	 * Begin the stream, sending a populated header
	 * @param header populated header.  Must include at least emergency rating durations and
	 *               power system resources
	 */
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

		this.windowBegin = header.getBegins();

	}

	/**
	 * Begin writing a new group of ratings for a particular resource.
	 * @param resourceId resource ID to write
	 */
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

	/**
	 * Fluent way of constructing valid forecast periods.  Use of these builders
	 * is the recommended way to send ratings for a given period.
	 * @return instance of a {@link ForecastPeriodBuilder} to create a new period.
	 */
	public ForecastPeriodBuilder periodBuilder() {
		if(windowBegin == null) {
			throw new IllegalStateException("Cannot write periods before submitting the header");
		}
		return new ForecastPeriodBuilderImpl(windowBegin,
				defaultIntervalMinutes,
				this);
	}


	/**
	 * <p>Write out a rating set, using the JSON format in a relatively raw way.  </p>
	 * <p><b>NOTE:</b> this method is only intended for advanced users with a low-level
	 * understanding of the schema.  {@link #periodBuilder()} is preferred for most users.</p>
	 * @param forecastRatingPeriod per-period rating set applying to the last
	 *                                resource set in {@link #beginResource(String)}.
	 */
	public void period(ForecastRatingPeriod forecastRatingPeriod) {
		checkCanWrite();
		try {
			validateScope(Scope.RATING, Scope.RATING);
			jsonGenerator.writeObject(forecastRatingPeriod);
		} catch (Exception e) {
			handleWriteError(e);
		}
	}

	/**
	 * Finish the resource set started with {@link #beginResource(String)}.
	 */
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

	/**
	 * Finalize the request.
	 * @return status of the proposal retrieved from the TROLIE server.
	 * <b>NOTE: TROLIE allows for partial proposal updates.</b>  Be sure to check
	 * and appropriately log any validation errors returned with this status, as they will
	 * indicate that ratings were not sent successfully for those resources.
	 */
	@Override
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

	/**
	 * Close underlying resources
	 */
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
