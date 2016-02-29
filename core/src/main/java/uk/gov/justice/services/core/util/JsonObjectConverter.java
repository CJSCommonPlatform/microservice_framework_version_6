package uk.gov.justice.services.core.util;

import uk.gov.justice.services.messaging.DefaultEnvelope;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * A utility class to manipulate JsonObject.
 */
@ApplicationScoped
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

    /**
     * Converts a jsonObject into {@link Envelope}
     *
     * @param envelopeJsonObject jsonObject that needs to be converted into Envelope.
     * @return An envelope corresponding to the <code>envelopeJsonObject</code>
     */
    public Envelope asEnvelope(final JsonObject envelopeJsonObject) {

        return DefaultEnvelope.envelopeFrom(JsonObjectMetadata.metadataFrom(envelopeJsonObject.getJsonObject(METADATA)),
                extractPayloadFromEnvelope(envelopeJsonObject));
    }

    /**
     * Converts an {@link Envelope} into a {@link JsonObject}
     *
     * @param envelope Envelope that needs to be converted.
     * @return a jsonObject corresponding to the <code>envelope</code>
     */
    public JsonObject fromEnvelope(final Envelope envelope) {

        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add(METADATA, envelope.metadata().asJsonObject());
        JsonObject payloadAsJsonObject = envelope.payload();
        payloadAsJsonObject.keySet().stream().forEach(key -> builder.add(key, payloadAsJsonObject.get(key)));

        return builder.build();
    }

    public JsonObject extractPayloadFromEnvelope(final JsonObject envelope) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        envelope.keySet().stream().filter(key -> !METADATA.equals(key)).forEach(key -> builder.add(key, envelope.get(key)));
        return builder.build();
    }

}
