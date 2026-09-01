package energy.trolie.client.model.monitoringsets;

import java.util.List;

import energy.trolie.client.model.common.DataProvenance;
import energy.trolie.client.model.common.PowerSystemResource;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.NoArgsConstructor;

/**
 * Monitoring set definition and its contents.  See
 * <a href="https://trolie.energy/concepts.html#monitoring-sets">Monitoring Sets</a>.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
@ToString
public class MonitoringSet {

    @JsonProperty("source")
    private DataProvenance source;

    @JsonProperty("id")
    private String id;

    @JsonProperty("description")
    private String description;

    @JsonProperty("power-system-resources")
    private List<PowerSystemResource> powerSystemResources;
}
