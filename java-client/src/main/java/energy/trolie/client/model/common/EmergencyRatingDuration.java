package energy.trolie.client.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Named duration of emergency ratings.
 */
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class EmergencyRatingDuration {

    @JsonProperty("name")
    private String name;

    @JsonProperty("duration-minutes")
    private int durationMinutes;

}
