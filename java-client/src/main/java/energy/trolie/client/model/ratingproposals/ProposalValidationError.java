package energy.trolie.client.model.ratingproposals;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Validation error for proposal data for a particular power system resource
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class ProposalValidationError {

    @JsonProperty("message")
    private String message;

    @JsonProperty("resource-id")
    private String resourceId;
}
