package energy.trolie.client.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Collection;

/**
 * Description of a power system resource, along with its list of alternate identifiers.
 */
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
@ToString
public class PowerSystemResource {

    @JsonProperty("resource-id")
    private String resourceId;

    @JsonProperty("alternate-identifiers")
    private Collection<AlternateIdentifier> alternateIdentifiers;

}
