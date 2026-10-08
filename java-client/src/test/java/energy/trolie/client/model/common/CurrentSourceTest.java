package energy.trolie.client.model.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import energy.trolie.client.model.ratingproposals.RealTimeRating;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CurrentSourceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializes_to_lowercase_json_value() throws IOException {
        assertEquals("\"telemetered\"", objectMapper.writeValueAsString(CurrentSource.TELEMETERED));
        assertEquals("\"calculated\"", objectMapper.writeValueAsString(CurrentSource.CALCULATED));
        assertEquals("\"estimated\"", objectMapper.writeValueAsString(CurrentSource.ESTIMATED));
        assertEquals("\"manual\"", objectMapper.writeValueAsString(CurrentSource.MANUAL));
    }

    @Test
    void deserializes_from_lowercase_json_value() throws IOException {
        assertEquals(CurrentSource.TELEMETERED, objectMapper.readValue("\"telemetered\"", CurrentSource.class));
        assertEquals(CurrentSource.CALCULATED, objectMapper.readValue("\"calculated\"", CurrentSource.class));
        assertEquals(CurrentSource.ESTIMATED, objectMapper.readValue("\"estimated\"", CurrentSource.class));
        assertEquals(CurrentSource.MANUAL, objectMapper.readValue("\"manual\"", CurrentSource.class));
    }

    @Test
    void field_is_optional_on_real_time_rating() throws IOException {
        RealTimeRating withoutCurrentSource = RealTimeRating.builder()
                .resourceId("resource1")
                .continuousOperatingLimit(RatingValue.fromMva(100f))
                .build();

        String json = objectMapper.writeValueAsString(withoutCurrentSource);
        RealTimeRating roundTripped = objectMapper.readValue(json, RealTimeRating.class);

        assertNull(roundTripped.getCurrentSource());
    }

    @Test
    void field_round_trips_on_real_time_rating() throws IOException {
        RealTimeRating withCurrentSource = RealTimeRating.builder()
                .resourceId("resource1")
                .continuousOperatingLimit(RatingValue.fromMva(100f))
                .currentSource(CurrentSource.MANUAL)
                .build();

        String json = objectMapper.writeValueAsString(withCurrentSource);
        RealTimeRating roundTripped = objectMapper.readValue(json, RealTimeRating.class);

        assertEquals(CurrentSource.MANUAL, roundTripped.getCurrentSource());
    }
}
