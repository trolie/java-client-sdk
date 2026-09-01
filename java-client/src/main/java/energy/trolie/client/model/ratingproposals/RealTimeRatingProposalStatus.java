package energy.trolie.client.model.ratingproposals;

import java.util.List;

import energy.trolie.client.model.common.DataProvenance;
import energy.trolie.client.model.common.PowerSystemResource;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Status of a real-time proposal
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class RealTimeRatingProposalStatus {

    @JsonProperty("source")
    private DataProvenance source;

    @JsonProperty("incomplete-obligation-count")
    private long incompleteObligationCount;

    @JsonProperty("incomplete-obligations")
    private List<PowerSystemResource> incompleteObligations;

    @JsonProperty("invalid-proposal-count")
    private long invalidProposalCount;

    @JsonProperty("proposal-validation-errors")
    private List<ProposalValidationError> proposalValidationErrors;
}
