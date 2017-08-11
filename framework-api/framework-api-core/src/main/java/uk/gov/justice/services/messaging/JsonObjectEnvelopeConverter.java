package uk.gov.justice.services.messaging;

import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * A converter class to convert between {@link JsonEnvelope} and {@link JsonObject}.
 */
public interface JsonObjectEnvelopeConverter {

    /**
     * Converts a jsonObject into {@link JsonEnvelope}
     *
     * @param envelopeJsonObject jsonObject that needs to be converted into JsonEnvelope.
     * @return An envelope corresponding to the <code>envelopeJsonObject</code>
     */
    JsonEnvelope asEnvelope(final JsonObject envelopeJsonObject);

    /**
     * Converts Json represented as a String into {@link JsonEnvelope}
     *
     * @param jsonString Json String that needs to be converted into JsonEnvelope
     * @return An envelope corresponding to the jsonObject String
     */
    JsonEnvelope asEnvelope(final String jsonString);

    /**
     * Converts an {@link JsonEnvelope} into a {@link JsonObject}
     *
     * @param envelope JsonEnvelope (with metadata) that needs to be converted.
     * @return a jsonObject corresponding to the <code>envelope</code>
     */
    JsonObject fromEnvelope(final JsonEnvelope envelope);

    /**
     * Extracts payload from the {@link JsonObject} representation of the provided envelope.
     *
     * @param envelope in {@link JsonObject} form.
     * @return the payload as {@link JsonValue}
     */
    JsonValue extractPayloadFromEnvelope(final JsonObject envelope);

    /**
     * Serialise a JSON envelope into a JSON string.
     *
     * @param envelope the envelope to serialise
     * @return the JSON
     */
    String asJsonString(final JsonEnvelope envelope);
}
