package org.trolie.client.impl.model.common;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.trolie.client.model.common.RatingValue;

import java.io.IOException;
import java.util.Map;

/**
 * Allows for naive Jackson Deserialization of
 * {@link RatingValue}
 */
public class RatingValueDeserializer extends StdDeserializer<RatingValue> {

    public RatingValueDeserializer() {
        super(RatingValue.class);
    }


    @Override
    public RatingValue deserialize(JsonParser jsonParser,
                                   DeserializationContext deserializationContext) throws IOException, JacksonException {

        Map<String, Float> marshalledJson =
                jsonParser.readValueAs(new TypeReference<Map<String, Float>>() {});
        if(marshalledJson == null) return null;
        return RatingValue.ofMappedJson(marshalledJson);
    }
}
