package energy.trolie.client.model.temporaryaarexceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import energy.trolie.client.model.common.DataProvenance;
import energy.trolie.client.model.common.EmergencyRatingValue;
import energy.trolie.client.model.common.PowerSystemResource;
import energy.trolie.client.model.common.RatingValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.List;

/**
 * <p>Represents a Temporary AAR Exception against a power system resource, as
 * returned by the TROLIE server.  See
 * <a href="https://trolie.energy/spec-1.0#tag/Temporary-AAR-Exceptions">Temporary AAR Exceptions</a>.</p>
 * <p>Includes a server-assigned {@link #id}, along with the {@link #source}
 * data provenance that allows a Temporary AAR Exception to be tracked across systems
 * that exchange it, such as when a Transmission Owner and a Reliability Coordinator
 * each maintain their own copy of the same exception.</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
@ToString
public class TemporaryAARException {

    @JsonProperty("id")
    private String id;

    @JsonProperty("source")
    private DataProvenance source;

    @JsonProperty("resource")
    private PowerSystemResource resource;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("start-time")
    private Instant startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("end-time")
    private Instant endTime;

    @JsonProperty("continuous-operating-limit")
    private RatingValue continuousOperatingLimit;

    @JsonProperty("emergency-operating-limits")
    private List<EmergencyRatingValue> emergencyOperatingLimits;

    @JsonProperty("reason")
    private String reason;

}
