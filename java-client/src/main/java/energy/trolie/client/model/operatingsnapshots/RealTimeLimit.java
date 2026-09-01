package energy.trolie.client.model.operatingsnapshots;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import energy.trolie.client.model.common.EmergencyRatingValue;
import energy.trolie.client.model.common.RatingValue;

import java.util.List;

/**
 * Real-time limit value set for a given resource.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class RealTimeLimit {

    @JsonProperty("resource-id")
    private String resourceId;

    @JsonProperty("continuous-operating-limit")
    private RatingValue continuousOperatingLimit;

    @JsonProperty("emergency-operating-limits")
    private List<EmergencyRatingValue> emergencyOperatingLimits;

}
