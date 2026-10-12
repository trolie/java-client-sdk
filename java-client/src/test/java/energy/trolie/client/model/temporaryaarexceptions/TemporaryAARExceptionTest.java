package energy.trolie.client.model.temporaryaarexceptions;

import energy.trolie.client.model.common.DataProvenance;
import energy.trolie.client.model.common.EmergencyRatingValue;
import energy.trolie.client.model.common.PowerSystemResource;
import energy.trolie.client.model.common.RatingValue;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemporaryAARExceptionTest {

    private final JsonMapper jsonMapper = new JsonMapper();

    private TemporaryAARException fullException() {
        return TemporaryAARException.builder()
                .id("46f7212b-1633-4c30-ba71-c6e987b2ded7")
                .source(DataProvenance.builder()
                        .provider("X-AMPL")
                        .originId("//trolie.example.com/temporary-aar-exceptions/46f7212b-1633-4c30-ba71-c6e987b2ded7")
                        .lastUpdated(Instant.parse("2023-07-12T22:05:43.044267100Z"))
                        .build())
                .resource(PowerSystemResource.of("8badf00d", List.of()))
                .startTime(Instant.parse("2025-07-12T23:00:00Z"))
                .endTime(Instant.parse("2025-07-13T19:00:00Z"))
                .continuousOperatingLimit(RatingValue.fromMva(160f))
                .emergencyOperatingLimits(List.of(
                        EmergencyRatingValue.of("emergency", RatingValue.fromMva(165f)),
                        EmergencyRatingValue.of("load-shed", RatingValue.fromMva(170f))))
                .reason("High wildfire risk forecasted until mid-day 7/13/25")
                .build();
    }

    @Test
    void serializes_using_hyphenated_field_names() throws IOException {
        String json = jsonMapper.writeValueAsString(fullException());

        assertTrue(json.contains("\"start-time\""));
        assertTrue(json.contains("\"end-time\""));
        assertTrue(json.contains("\"continuous-operating-limit\""));
        assertTrue(json.contains("\"emergency-operating-limits\""));
        assertTrue(json.contains("\"origin-id\""));
        assertTrue(json.contains("\"last-updated\""));
        assertTrue(json.contains("\"resource-id\""));
    }

    @Test
    void round_trips_through_json() throws IOException {
        TemporaryAARException original = fullException();

        String json = jsonMapper.writeValueAsString(original);
        TemporaryAARException roundTripped = jsonMapper.readValue(json, TemporaryAARException.class);

        assertEquals(original, roundTripped);
    }

    @Test
    void deserializes_from_spec_example() throws IOException {
        String json = """
                {
                  "source": {
                    "provider": "X-AMPL",
                    "last-updated": "2023-07-12T15:05:43.044267100-07:00",
                    "origin-id": "//trolie.example.com/temporary-aar-exceptions/46f7212b-1633-4c30-ba71-c6e987b2ded7"
                  },
                  "id": "46f7212b-1633-4c30-ba71-c6e987b2ded7",
                  "resource": {
                    "resource-id": "8badf00d",
                    "alternate-identifiers": [
                      { "name": "segmentX", "authority": "TO-NERC-ID" },
                      { "name": "LINE1 SEG-X", "authority": "RC-NERC-ID", "mrid": "8badf00d" }
                    ]
                  },
                  "start-time": "2025-07-12T16:00:00-07:00",
                  "end-time": "2025-07-13T12:00:00-07:00",
                  "continuous-operating-limit": { "mva": "160" },
                  "emergency-operating-limits": [
                    { "duration-name": "emergency", "limit": { "mva": "165" } },
                    { "duration-name": "load-shed", "limit": { "mva": "170" } }
                  ],
                  "reason": "High wildfire risk forecasted until mid-day 7/13/25"
                }
                """;

        TemporaryAARException value = jsonMapper.readValue(json, TemporaryAARException.class);

        assertEquals("46f7212b-1633-4c30-ba71-c6e987b2ded7", value.getId());
        assertEquals("X-AMPL", value.getSource().getProvider());
        assertEquals("8badf00d", value.getResource().getResourceId());
        assertEquals(2, value.getResource().getAlternateIdentifiers().size());
        assertEquals(160f, value.getContinuousOperatingLimit().getMVA());
        assertEquals(2, value.getEmergencyOperatingLimits().size());
        assertEquals("emergency", value.getEmergencyOperatingLimits().get(0).getDurationName());
        assertEquals("High wildfire risk forecasted until mid-day 7/13/25", value.getReason());
    }

    @Test
    void end_time_is_optional() throws IOException {
        TemporaryAARException withoutEndTime = TemporaryAARException.builder()
                .id("46f7212b-1633-4c30-ba71-c6e987b2ded7")
                .resource(PowerSystemResource.of("8badf00d", List.of()))
                .startTime(Instant.parse("2025-07-12T23:00:00Z"))
                .continuousOperatingLimit(RatingValue.fromMva(160f))
                .emergencyOperatingLimits(List.of(EmergencyRatingValue.of("emergency", RatingValue.fromMva(165f))))
                .build();

        String json = jsonMapper.writeValueAsString(withoutEndTime);
        TemporaryAARException roundTripped = jsonMapper.readValue(json, TemporaryAARException.class);

        assertNull(roundTripped.getEndTime());
    }

}
