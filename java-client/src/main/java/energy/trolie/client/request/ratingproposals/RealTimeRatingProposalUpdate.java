package energy.trolie.client.request.ratingproposals;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import energy.trolie.client.exception.TrolieException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import energy.trolie.client.TrolieApiConstants;
import energy.trolie.client.impl.request.AbstractStreamingUpdate;
import energy.trolie.client.model.ratingproposals.ProposalHeader;
import energy.trolie.client.model.ratingproposals.RealTimeRating;
import energy.trolie.client.model.ratingproposals.RealTimeRatingProposalStatus;

import java.util.Map;
import java.util.function.Function;

/**
 * <p>Streaming update for real-time rating proposals.  Users stream out proposals by invoking methods
 * in the following sequence:</p>
 * <ol>
 *     <li>{@link #begin(ProposalHeader)} with a completely populated header.</li>
 *     <li>{@link #rating(RealTimeRating)} for rating value set.</li>
 *     <li>{@link #complete()} to synchronously finish the request.</li>
 * </ol>
 */
public class RealTimeRatingProposalUpdate extends AbstractStreamingUpdate<RealTimeRatingProposalStatus> {

	private static final Logger logger = LoggerFactory.getLogger(RealTimeRatingProposalUpdate.class);

	/**
	 * Constructs new update.  Should generally not be called by application code
	 * @param httpClient client
	 * @param host host
	 * @param requestConfig client request config
	 * @param bufferSize internal buffer size
	 * @param objectMapper Jackson object mapper
	 * @param httpHeader mapped header list
	 */
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

	/**
	 * Begin the stream, sending a populated header
	 * @param header populated header.  Must include at least emergency rating durations and
	 *               power system resources
	 */
	public void begin(ProposalHeader header) {

		validateScope(Scope.MAIN, Scope.BEGIN);
		try {
			jsonGenerator = objectMapper.createGenerator(createRequestOutputStream());
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

	/**
	 * Send a rating value set for a given resource.
	 * @param rating value set
	 */
	public void rating(RealTimeRating rating) {
		checkCanWrite();
		try {
			validateScope(Scope.RATING, Scope.RATING, Scope.MAIN);
			jsonGenerator.writeObject(rating);
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
