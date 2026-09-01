package energy.trolie.client.model.ratingproposals;

import java.util.List;

import energy.trolie.client.model.common.DataProvenance;
import energy.trolie.client.model.common.EmergencyRatingDuration;
import energy.trolie.client.model.common.PowerSystemResource;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Header information for rating proposals submitted to TROLIE.  Users must enter at least the assumed emergency
 * durations and power system resources
 */
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@EqualsAndHashCode
public class ProposalHeader {

    @JsonProperty("source")
    private DataProvenance source;

    @JsonProperty("default-emergency-durations")
    private List<EmergencyRatingDuration> defaultEmergencyRatingDurations;

    @JsonProperty("power-system-resources")
    private List<PowerSystemResource> powerSystemResources;


}
