package uk.gov.justice.services.common.converter;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import java.io.StringWriter;

/**
 * Converts JsonObject to Json as a String.
 */
public class JsonObjectToStringConverter implements Converter<JsonObject, String> {

    @Override
    public String convert(final JsonObject source) {
        final StringWriter stringWriter = new StringWriter();
        try (final JsonWriter writer = Json.createWriter(stringWriter)) {
            writer.writeObject(source);
        }

        return stringWriter.getBuffer().toString();
    }

}
