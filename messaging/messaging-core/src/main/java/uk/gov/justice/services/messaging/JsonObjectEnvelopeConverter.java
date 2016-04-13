package uk.gov.justice.services.messaging;


import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * A converter class to convert between {@link JsonEnvelope} and {@link JsonObject}.
 */
public class JsonObjectEnvelopeConverter {

    public static final String METADATA = "_metadata";

    /**
     * Converts a jsonObject into {@link JsonEnvelope}
     *
     * @param envelopeJsonObject jsonObject that needs to be converted into JsonEnvelope.
     * @return An envelope corresponding to the <code>envelopeJsonObject</code>
     */
    public JsonEnvelope asEnvelope(final JsonObject envelopeJsonObject) {

        return DefaultJsonEnvelope.envelopeFrom(JsonObjectMetadata.metadataFrom(envelopeJsonObject.getJsonObject(METADATA)),
                extractPayloadFromEnvelope(envelopeJsonObject));
    }

    /**
     * Converts an {@link JsonEnvelope} into a {@link JsonObject}
     *
     * @param envelope JsonEnvelope that needs to be converted.
     * @return a jsonObject corresponding to the <code>envelope</code>
     */
    public JsonObject fromEnvelope(final JsonEnvelope envelope) {

        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add(METADATA, envelope.metadata().asJsonObject());
        JsonObject payloadAsJsonObject = envelope.payloadAsJsonObject();
        payloadAsJsonObject.keySet().stream().forEach(key -> builder.add(key, payloadAsJsonObject.get(key)));

        return builder.build();
    }

    /**
     * Extracts payload from the {@link JsonObject} representation of the provided envelope.
     *
     * @param envelope in {@link JsonObject} form.
     * @return the payload as {@link JsonObject}
     */
    public JsonObject extractPayloadFromEnvelope(final JsonObject envelope) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        envelope.keySet().stream().filter(key -> !METADATA.equals(key)).forEach(key -> builder.add(key, envelope.get(key)));
        return builder.build();
    }

}
