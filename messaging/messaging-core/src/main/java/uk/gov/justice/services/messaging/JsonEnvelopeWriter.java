package uk.gov.justice.services.messaging;

import static javax.json.Json.createWriterFactory;
import static javax.json.stream.JsonGenerator.PRETTY_PRINTING;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;

/**
 * Writer class to pretty print Json.
 */
public class JsonEnvelopeWriter {

    private static final JsonWriterFactory writerFactory;

    static {
        final Map<String, Object> properties = new HashMap<>(1);
        properties.put(PRETTY_PRINTING, true);
        writerFactory = createWriterFactory(properties);
    }

    public static String writeJsonObject(final JsonObject json) {
        try (final StringWriter swriter = new StringWriter();
             final JsonWriter jsonWriter = writerFactory.createWriter(swriter)) {
            jsonWriter.writeObject(json);
            return swriter.toString();
        } catch (IOException e) {
            return "Failed to write Json: " + e.getMessage();
        }
    }
}
