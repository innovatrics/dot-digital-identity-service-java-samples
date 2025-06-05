package com.innovatrics.integrationsamples.testhelper;

import com.google.gson.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import java.lang.reflect.Type;

/**
 * DataTypeDeserializer is a custom deserializer for byte arrays in JSON.
 *
 * This class implements the JsonDeserializer interface provided by Gson,
 * enabling the deserialization of encoded Base64 strings back into byte arrays.
 * Implements JsonDeserializer<byte[]>.
 */
public class DataTypeDeserializer implements JsonDeserializer<byte[]> {
    @Override
    public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json instanceof JsonPrimitive && ((JsonPrimitive) json).isString()) {
            return Base64.getDecoder().decode(json.getAsString());
        } else {
            throw new JsonParseException("Unexpected JSON type: " + json.getClass().getSimpleName());
        }
    }
}
