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
 * <p>Represents a request to create or update a Temporary AAR Exception against
 * a power system resource.  See
 * <a href="https://trolie.energy/spec-1.0#tag/Temporary-AAR-Exceptions">Temporary AAR Exceptions</a>.</p>
 * <p>Unlike {@link TemporaryAARException}, this does not include a server-assigned
 * {@code id}, since that is either not yet known (on create) or is instead conveyed
 * out-of-band as part of the request path (on update).</p>
 * <p>Setting the {@link #source} field allows API users to convey the
 * provenance of a Temporary AAR Exception that originated in another system,
 * such as when replicating a Temporary AAR Exception created by a Transmission
 * Owner into a Reliability Coordinator's TROLIE instance.</p>
 * <p>Termination of a Temporary AAR Exception is represented by submitting an
 * update (i.e. a TROLIE {@code PUT}) with an earlier {@link #endTime}.
 * TROLIE does not define a separate operation for termination.</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
@ToString
public class TemporaryAARExceptionRequest {

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
