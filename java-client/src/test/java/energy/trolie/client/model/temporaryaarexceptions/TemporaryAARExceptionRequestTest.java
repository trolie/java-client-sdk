package energy.trolie.client.model.temporaryaarexceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import energy.trolie.client.model.common.DataProvenance;
import energy.trolie.client.model.common.EmergencyRatingValue;
import energy.trolie.client.model.common.PowerSystemResource;
import energy.trolie.client.model.common.RatingValue;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class TemporaryAARExceptionRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void does_not_include_an_id_field() throws IOException {
        // id is server-assigned and only present on the TemporaryAARException response, never on the request.
        TemporaryAARExceptionRequest request = TemporaryAARExceptionRequest.builder()
                .resource(PowerSystemResource.of("8badf00d", List.of()))
                .startTime(Instant.parse("2025-07-12T23:00:00Z"))
                .continuousOperatingLimit(RatingValue.fromMva(160f))
                .emergencyOperatingLimits(List.of(EmergencyRatingValue.of("emergency", RatingValue.fromMva(165f))))
                .build();

        String json = objectMapper.writeValueAsString(request);

        assertFalse(json.contains("\"id\""));
    }

    @Test
    void round_trips_with_source_for_peer_replication() throws IOException {
        TemporaryAARExceptionRequest request = TemporaryAARExceptionRequest.builder()
                .source(DataProvenance.builder()
                        .provider("X-AMPL")
                        .originId("//trolie.example.com/temporary-aar-exceptions/46f7212b-1633-4c30-ba71-c6e987b2ded7")
                        .lastUpdated(Instant.parse("2023-07-12T22:05:43.044267100Z"))
                        .build())
                .resource(PowerSystemResource.of("8badf00d", List.of()))
                .startTime(Instant.parse("2025-07-12T23:00:00Z"))
                .endTime(Instant.parse("2025-07-13T19:00:00Z"))
                .continuousOperatingLimit(RatingValue.fromMva(160f))
                .emergencyOperatingLimits(List.of(EmergencyRatingValue.of("emergency", RatingValue.fromMva(165f))))
                .reason("High wildfire risk forecasted until mid-day 7/13/25")
                .build();

        String json = objectMapper.writeValueAsString(request);
        TemporaryAARExceptionRequest roundTripped = objectMapper.readValue(json, TemporaryAARExceptionRequest.class);

        assertEquals(request, roundTripped);
        assertEquals("X-AMPL", roundTripped.getSource().getProvider());
    }

    @Test
    void source_is_optional() throws IOException {
        TemporaryAARExceptionRequest request = TemporaryAARExceptionRequest.builder()
                .resource(PowerSystemResource.of("8badf00d", List.of()))
                .startTime(Instant.parse("2025-07-12T23:00:00Z"))
                .continuousOperatingLimit(RatingValue.fromMva(160f))
                .emergencyOperatingLimits(List.of(EmergencyRatingValue.of("emergency", RatingValue.fromMva(165f))))
                .build();

        String json = objectMapper.writeValueAsString(request);
        TemporaryAARExceptionRequest roundTripped = objectMapper.readValue(json, TemporaryAARExceptionRequest.class);

        assertNull(roundTripped.getSource());
    }

}
