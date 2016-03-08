package uk.gov.justice.services.common.converter;


import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * A utility class to manipulate JsonObject.
 */
public class JsonObjectConverter {

    public static final String METADATA = "_metadata";

    /**
     * Converts a json string into a JsonObject.
     *
     * @param jsonString A String containing a valid json object.
     * @return the corresponding JsonObject.
     */
    public JsonObject fromString(final String jsonString) {
        JsonObject jsonObject;
        try (JsonReader reader = Json.createReader(new StringReader(jsonString))) {
            jsonObject = reader.readObject();
        }

        return jsonObject;
    }

    /**
     * Converts a JsonObject into a valid json string.
     *
     * @param jsonObject jsonObject to be converted.
     * @return String representation of the <code>jsonObject</code>.
     */
    public String asString(final JsonObject jsonObject) {
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter writer = Json.createWriter(stringWriter)) {
            writer.writeObject(jsonObject);
        }

        return stringWriter.getBuffer().toString();
    }


}
