package uk.gov.justice.services.messaging;


import static javax.json.Json.createObjectBuilder;
import static javax.json.JsonValue.ValueType.OBJECT;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.METADATA;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

import java.io.StringReader;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A converter class to convert between {@link JsonEnvelope} and {@link JsonObject}.
 */
@ApplicationScoped
public class JsonObjectEnvelopeConverter {

    @Inject
    ObjectMapper objectMapper;

    /**
     * Converts a jsonObject into {@link JsonEnvelope}
     *
     * @param envelopeJsonObject jsonObject that needs to be converted into JsonEnvelope.
     * @return An envelope corresponding to the <code>envelopeJsonObject</code>
     */
    public JsonEnvelope asEnvelope(final JsonObject envelopeJsonObject) {
        return envelopeFrom(
                metadataFrom(envelopeJsonObject.getJsonObject(METADATA)),
                extractPayloadFromEnvelope(envelopeJsonObject));
    }
    public JsonEnvelope asEnvelope(final String jsonString) {
        return asEnvelope(Json.createReader(new StringReader(jsonString)).readObject());
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

        final JsonObjectBuilder builder = createObjectBuilder();
        builder.add(METADATA, metadata.asJsonObject());

        final ValueType payloadType = envelope.payload().getValueType();
        if (payloadType == OBJECT) {
            final JsonObject payloadAsJsonObject = envelope.payloadAsJsonObject();
            payloadAsJsonObject.keySet().stream().forEach(key -> builder.add(key, payloadAsJsonObject.get(key)));
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
        final JsonObjectBuilder builder = createObjectBuilder();
        envelope.keySet().stream().filter(key -> !METADATA.equals(key)).forEach(key -> builder.add(key, envelope.get(key)));
        return builder.build();
    }

    /**
     * Serialise a JSON envelope into a JSON string.
     *
     * @param envelope the envelope to serialise
     * @return the JSON
     */
    public String asJsonString(final JsonEnvelope envelope) {
        try {
            return objectMapper.writeValueAsString(fromEnvelope(envelope));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize JSON envelope", e);
        }
    }

}
