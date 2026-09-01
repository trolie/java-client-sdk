package energy.trolie.client.model.ratingproposals;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Batch real-time proposal.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class RealTimeRatingProposal {

    @JsonProperty("proposal-header")
    private ProposalHeader proposalHeader;

    @JsonProperty("ratings")
    private List<RealTimeRating> ratings;
}
