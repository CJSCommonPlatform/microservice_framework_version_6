package uk.gov.justice.services.messaging;


import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.OBJECT;

/**
 * A converter class to convert between {@link JsonEnvelope} and {@link JsonObject}.
 */
public class JsonObjectEnvelopeConverter {

    private static final String METADATA = "_metadata";
    private static final String RESULTS = "results";

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
     * @param envelope JsonEnvelope (with metadata) that needs to be converted.
     * @return a jsonObject corresponding to the <code>envelope</code>
     */
    public JsonObject fromEnvelope(final JsonEnvelope envelope) {
        final Metadata metadata = envelope.metadata();

        if (metadata == null) {
            throw new IllegalArgumentException("Failed to convert envelope, no metadata present.");
        }

        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add(METADATA, metadata.asJsonObject());

        final ValueType payloadType = envelope.payload().getValueType();
        if (payloadType == OBJECT) {
            final JsonObject payloadAsJsonObject = envelope.payloadAsJsonObject();
            payloadAsJsonObject.keySet().stream().forEach(key -> builder.add(key, payloadAsJsonObject.get(key)));
        } else if (payloadType == ARRAY) {
            builder.add(RESULTS, envelope.payload());
        } else {
            throw new IllegalArgumentException(String.format("Payload type %s not supported.", payloadType));
        }

        return builder.build();
    }

    /**
     * Extracts payload from the {@link JsonObject} representation of the provided envelope.
     *
     * @param envelope in {@link JsonObject} form.
     * @return the payload as {@link JsonValue}
     */
    public JsonValue extractPayloadFromEnvelope(final JsonObject envelope) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        envelope.keySet().stream().filter(key -> !METADATA.equals(key)).forEach(key -> builder.add(key, envelope.get(key)));
        return builder.build();
    }

}
