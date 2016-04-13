package uk.gov.justice.services.messaging;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * Default implementation of an envelope.
 */
public class DefaultJsonEnvelope implements JsonEnvelope {

    private Metadata metadata;

    private JsonValue payload;

    private DefaultJsonEnvelope(final Metadata metadata, final JsonValue payload) {
        this.metadata = metadata;
        this.payload = payload;
    }

    public static JsonEnvelope envelopeFrom(final Metadata metadata, final JsonValue payload) {
        return new DefaultJsonEnvelope(metadata, payload);
    }

    @Override
    public Metadata metadata() {
        return metadata;
    }

    @Override
    public JsonValue payload() {
        return payload;
    }

    @Override
    public JsonObject payloadAsJsonObject() {
        return (JsonObject) payload;
    }

    @Override
    public JsonArray payloadAsJsonArray() {
        return (JsonArray) payload;
    }

    @Override
    public JsonNumber payloadAsJsonNumber() {
        return (JsonNumber) payload;
    }

    @Override
    public JsonString payloadAsJsonString() {
        return (JsonString) payload;
    }


}
