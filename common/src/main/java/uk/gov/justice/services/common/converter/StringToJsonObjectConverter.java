package uk.gov.justice.services.common.converter;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

/**
 * Converts a Json String to a JsonObject.
 */
public class StringToJsonObjectConverter implements Converter<String, JsonObject> {

    @Override
    public JsonObject convert(final String source) {
        JsonObject jsonObject;
        try (final JsonReader reader = Json.createReader(new StringReader(source))) {
            jsonObject = reader.readObject();
        }

        return jsonObject;
    }

}
