package energy.trolie.client.model.operatingsnapshots;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import energy.trolie.client.model.common.DataProvenance;
import energy.trolie.client.model.common.EmergencyRatingDuration;
import energy.trolie.client.model.common.PowerSystemResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Header information for rating snapshots downloaded from TROLIE.  Includes
 * various metadata on the snapshot itself, the emergency ratings used as well as the included
 * power system resources.
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@EqualsAndHashCode
public abstract class SnapshotHeader {

    @JsonProperty("source")
    private DataProvenance source;

    @Builder.Default
    @JsonProperty("power-system-resources")
    private List<PowerSystemResource> powerSystemResources = new ArrayList<>();

    @JsonProperty("default-emergency-durations")
    private List<EmergencyRatingDuration> defaultEmergencyRatingDurations;
}
