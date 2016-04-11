package uk.gov.justice.services.common.converter;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * Converts a Json String to a JsonObject.
 */
public class StringToJsonObjectConverter implements Converter<String, JsonObject> {

    @Override
    public JsonObject convert(final String source) {
        try (final JsonReader reader = Json.createReader(new StringReader(source))) {
            return reader.readObject();
        }
    }

}
