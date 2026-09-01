package energy.trolie.client.model.operatingsnapshots;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * {@link SnapshotHeader} for forecast ratings, which includes a window begin period.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
public class ForecastSnapshotHeader extends SnapshotHeader {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("begins")
    private Instant begins;
}
