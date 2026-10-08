package energy.trolie.client.model.common;

import energy.trolie.client.model.ratingproposals.RealTimeRating;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CurrentSourceTest {

    private final JsonMapper jsonMapper = new JsonMapper();

    @Test
    void serializes_to_lowercase_json_value() throws IOException {
        assertEquals("\"telemetered\"", jsonMapper.writeValueAsString(CurrentSource.TELEMETERED));
        assertEquals("\"calculated\"", jsonMapper.writeValueAsString(CurrentSource.CALCULATED));
        assertEquals("\"estimated\"", jsonMapper.writeValueAsString(CurrentSource.ESTIMATED));
        assertEquals("\"manual\"", jsonMapper.writeValueAsString(CurrentSource.MANUAL));
    }

    @Test
    void deserializes_from_lowercase_json_value() throws IOException {
        assertEquals(CurrentSource.TELEMETERED, jsonMapper.readValue("\"telemetered\"", CurrentSource.class));
        assertEquals(CurrentSource.CALCULATED, jsonMapper.readValue("\"calculated\"", CurrentSource.class));
        assertEquals(CurrentSource.ESTIMATED, jsonMapper.readValue("\"estimated\"", CurrentSource.class));
        assertEquals(CurrentSource.MANUAL, jsonMapper.readValue("\"manual\"", CurrentSource.class));
    }

    @Test
    void field_is_optional_on_real_time_rating() throws IOException {
        RealTimeRating withoutCurrentSource = RealTimeRating.builder()
                .resourceId("resource1")
                .continuousOperatingLimit(RatingValue.fromMva(100f))
                .build();

        String json = jsonMapper.writeValueAsString(withoutCurrentSource);
        RealTimeRating roundTripped = jsonMapper.readValue(json, RealTimeRating.class);

        assertNull(roundTripped.getCurrentSource());
    }

    @Test
    void field_round_trips_on_real_time_rating() throws IOException {
        RealTimeRating withCurrentSource = RealTimeRating.builder()
                .resourceId("resource1")
                .continuousOperatingLimit(RatingValue.fromMva(100f))
                .currentSource(CurrentSource.MANUAL)
                .build();

        String json = jsonMapper.writeValueAsString(withCurrentSource);
        RealTimeRating roundTripped = jsonMapper.readValue(json, RealTimeRating.class);

        assertEquals(CurrentSource.MANUAL, roundTripped.getCurrentSource());
    }
}
