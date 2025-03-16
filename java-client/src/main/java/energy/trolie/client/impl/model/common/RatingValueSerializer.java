package energy.trolie.client.impl.model.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import energy.trolie.client.model.common.RatingValue;

import java.io.IOException;

/**
 * Allows naive Jackson Serialization of rating values.
 * {@link RatingValue}
 */
public class RatingValueSerializer extends StdSerializer<RatingValue> {
    public RatingValueSerializer() {
        super(RatingValue.class);
    }

    @Override
    public void serialize(RatingValue ratingValue,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        // We just serialize the map.
        jsonGenerator.writeObject(ratingValue.getValues());
    }
}
