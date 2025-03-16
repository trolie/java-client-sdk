package org.trolie.client.impl.model.ratingproposals;

import lombok.NonNull;
import org.trolie.client.model.ratingproposals.ForecastPeriodBuilder;
import org.trolie.client.model.ratingproposals.ForecastRatingPeriod;
import org.trolie.client.request.ratingproposals.ForecastRatingProposalUpdate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Private implementation of the builder.
 */
public class ForecastPeriodBuilderImpl
        extends AbstractRatingBuilder
        implements ForecastPeriodBuilder {


    private final @NonNull Instant forecastWindowBegin;
    private final int periodLengthMinutes;
    private Instant periodStart;
    private Instant periodEnd;
    private final ForecastRatingProposalUpdate update;

    public ForecastPeriodBuilderImpl(Instant forecastWindowBegin,
                                     int periodLengthMinutes,
                                     ForecastRatingProposalUpdate update) {
        this.forecastWindowBegin = forecastWindowBegin;
        this.periodLengthMinutes = periodLengthMinutes;
        this.update = update;
    }


    @Override
    public void setPeriod(int periodIndex) {
        // Assumes that this is a zero-based period derived from the period length.
        periodStart = forecastWindowBegin.plus((long) periodIndex * periodLengthMinutes,
                ChronoUnit.MINUTES);

    }

    @Override
    public void setPeriodStart(Instant periodStart) {
        this.periodStart = periodStart;
    }

    @Override
    public void setPeriodEnd(Instant periodEnd) {
        this.periodEnd = periodEnd;
    }

    @Override
    public void complete() {
        if(periodStart == null) {
            throw new IllegalStateException("Period must be defined before committing the period update");
        }

        if(periodEnd == null) {
            periodEnd = periodStart.plus(periodLengthMinutes, ChronoUnit.MINUTES);
        }

        update.period(ForecastRatingPeriod.builder()
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .inputsUsed(inputsUsed)
                .continuousOperatingLimit(continuousOperatingLimit)
                .emergencyOperatingLimits(getEmergencyRatings())
                .build());
    }
}
