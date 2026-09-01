package energy.trolie.client.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * Generic input value used to compute ratings, attached to proposals.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@Builder
@ToString
public class InputValue {

    @NonNull
    @JsonProperty("name")
    private String name;

    @NonNull
    @JsonProperty("value")
    private String value;

    @NonNull
    @JsonProperty("unit")
    private String unit;

}
