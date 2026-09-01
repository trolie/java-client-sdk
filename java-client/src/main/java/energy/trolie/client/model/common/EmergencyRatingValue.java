package energy.trolie.client.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Emergency limit value for a given duration.
 */
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class EmergencyRatingValue {

    @JsonProperty("duration-name")
    private String durationName;

    @JsonProperty("limit")
    private RatingValue limit;

}
