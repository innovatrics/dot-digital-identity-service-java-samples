package com.innovatrics.integrationsamples.testhelper;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.Base64;

/**
 * OffsetDateTimeDeserializer is a custom deserializer for OffsetDateTime in JSON.
 *
 * This class implements the JsonDeserializer interface provided by Gson,
 * enabling the deserialization of date-time strings back into OffsetDateTime.
 * Implements JsonDeserializer<OffsetDateTime>.
 */
public class OffsetDateTimeDeserializer implements JsonDeserializer<OffsetDateTime> {
    @Override
    public OffsetDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement instanceof JsonPrimitive && ((JsonPrimitive) jsonElement).isString()) {
            return OffsetDateTime.parse(jsonElement.getAsString());
        } else {
            throw new JsonParseException("Unexpected JSON type: " + jsonElement.getClass().getSimpleName());
        }
    }
}
