package energy.trolie.client.model.ratingproposals;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import energy.trolie.client.model.common.DataProvenance;
import energy.trolie.client.model.common.PowerSystemResource;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Status of a forecast rating proposal
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class ForecastRatingProposalStatus {

    @JsonProperty("source")
    private DataProvenance source;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("begins")
    private Instant begins;

    @JsonProperty("incomplete-obligation-count")
    private int incompleteObligationCount;

    @JsonProperty("incomplete-obligations")
    private List<PowerSystemResource> incompleteObligations;

    @JsonProperty("invalid-proposal-count")
    private int invalidProposalCount;

    @JsonProperty("proposal-validation-errors")
    private List<ProposalValidationError> proposalValidationErrors;
}
