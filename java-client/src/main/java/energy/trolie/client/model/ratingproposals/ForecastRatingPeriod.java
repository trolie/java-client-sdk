package energy.trolie.client.model.ratingproposals;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import energy.trolie.client.model.common.CurrentSource;
import energy.trolie.client.model.common.InputValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import energy.trolie.client.model.common.EmergencyRatingValue;
import energy.trolie.client.model.common.RatingValue;
import energy.trolie.client.model.common.LimitingAnalysis;
import java.time.Instant;
import java.util.List;

/**
 * Forecast rating value set for a given period
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class ForecastRatingPeriod {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("period-start")
    private Instant periodStart;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("period-end")
    private Instant periodEnd;

    @JsonProperty("continuous-operating-limit")
    private RatingValue continuousOperatingLimit;

    @JsonProperty("emergency-operating-limits")
    private List<EmergencyRatingValue> emergencyOperatingLimits;

    @JsonProperty("inputs-used")
    private List<InputValue> inputsUsed;

    /**
     * Optional origin/quality of the rating value, e.g. telemetered, calculated,
     * estimated, or manual.
     */
    @JsonProperty("current-source")
    private CurrentSource currentSource;

    @JsonProperty("limiting-analysis")
    private LimitingAnalysis limitingAnalysis;

}
