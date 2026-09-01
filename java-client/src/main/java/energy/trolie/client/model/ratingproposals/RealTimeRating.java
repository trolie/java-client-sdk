package energy.trolie.client.model.ratingproposals;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import energy.trolie.client.model.common.EmergencyRatingValue;
import energy.trolie.client.model.common.InputValue;
import energy.trolie.client.model.common.RatingValue;

import java.util.List;

/**
 * Real-time rating value set for a given resource
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@Builder
public class RealTimeRating {

    @JsonProperty("resource-id")
    private String resourceId;

    @JsonProperty("continuous-operating-limit")
    private RatingValue continuousOperatingLimit;

    @JsonProperty("emergency-operating-limits")
    private List<EmergencyRatingValue> emergencyOperatingLimits;

    @JsonProperty("inputs-used")
    private List<InputValue> inputsUsed;
}
