package energy.trolie.client.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Alternate identifier for a given power system resource
 */
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class AlternateIdentifier {

    @JsonProperty("name")
    private String name;
    
    @JsonProperty("type")
    private String type;

    @JsonProperty("authority")
    private String authority;

    @JsonProperty("mrid")
    private String mrid;
}
