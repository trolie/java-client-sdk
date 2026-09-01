package energy.trolie.client.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

/**
 * Provenance for a particular data set
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
@ToString
public class DataProvenance {


    @JsonProperty("provider")
    private String provider;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("last-updated")
    private Instant lastUpdated = Instant.now();

    @JsonProperty("origin-id")
    private String originId;

}
