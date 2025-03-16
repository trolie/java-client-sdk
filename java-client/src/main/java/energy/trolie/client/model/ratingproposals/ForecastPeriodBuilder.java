package energy.trolie.client.model.ratingproposals;

import energy.trolie.client.request.ratingproposals.ForecastRatingProposalUpdate;
import energy.trolie.client.TrolieClientBuilder;

import java.time.Instant;

/**
 * Rating value builder for specific forecast periods, emitting
 * {@link ForecastRatingPeriod}s.  Intended to be used in concert with
 * {@link ForecastRatingProposalUpdate}.
 */
public interface ForecastPeriodBuilder extends
        RatingBuilder {

    /**
     * <p>Convenience method to set the period as an offset from the forecast window
     * begin.  For example, assume the typical convention of hour-long periods.  
     * If the forecast window were to begin at 2025-07-12T00:00-07:00, 
     * then period 0 would start at 2025-07-12T00:00-07:00, period 1 would start
     * at 2025-07-12T01:00-07:00, and so on.</p>
     * <p>Use of this method supersedes, and is assumed to be mutually exclusive
     * with any invocations of {@link #setPeriodStart(Instant)} and 
     * {@link #setPeriodEnd(Instant)}</p>
     * @param periodIndex 0-based index of the period number within the snapshot.  
     *                    Uses the client configuration to determine the length of the period
     *                    
     */
    void setPeriod(int periodIndex);

    /**
     * Sets an absolute period start.
     * @param periodStart new period start value
     */
    void setPeriodStart(Instant periodStart);

    /**
     * Sets an absolute period end.  If not used, assumes the default
     * as configured through {@link TrolieClientBuilder#periodLength(int)}
     * @param periodEnd absolute period end.
     */
    void setPeriodEnd(Instant periodEnd);

}
