package energy.trolie.client.model.ratingproposals;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * {@link ProposalHeader} for forecasts.  Intended to be constructed via builder.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ForecastProposalHeader extends ProposalHeader {


    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("begins")
    private @NonNull Instant begins;

}
