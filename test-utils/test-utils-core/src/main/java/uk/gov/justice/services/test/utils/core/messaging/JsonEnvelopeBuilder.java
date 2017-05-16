package uk.gov.justice.services.test.utils.core.messaging;

import static javax.json.JsonValue.NULL;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;

import java.math.BigDecimal;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * Implementation of the {@link JsonEnvelope} specifically for testing purposes that include useful
 * builder options. For production code the Enveloper
 * should be used to create envelopes.
 */
public class JsonEnvelopeBuilder {

    private JsonObjectBuilderWrapper payload;
    private JsonObjectMetadata.Builder metadata;

    public static JsonEnvelope envelopeFrom(final Metadata metadata, final JsonValue payload) {
        return new DefaultJsonEnvelope(metadata, payload);
    }

    public static JsonEnvelope envelopeFrom(final JsonObjectMetadata.Builder metadataBuilder, final JsonValue payload) {
        return envelopeFrom(metadataBuilder.build(), payload);
    }

    public static JsonEnvelopeBuilder envelope() {
        return new JsonEnvelopeBuilder();
    }

    private JsonEnvelopeBuilder() {
        payload = new JsonObjectBuilderWrapper();
    }

    public JsonEnvelopeBuilder(final JsonEnvelope envelope) {
        payload = new JsonObjectBuilderWrapper(envelope.payloadAsJsonObject());
        this.metadata = metadataFrom(envelope.metadata());
    }

    public JsonEnvelopeBuilder with(final JsonObjectMetadata.Builder metadata) {
        this.metadata = metadata;
        return this;
    }

    public JsonEnvelopeBuilder withPayloadOf(final String value, final String... name) {
        payload.add(value, name);
        return this;
    }

    public JsonEnvelopeBuilder withPayloadOf(final UUID value, final String... name) {
        payload.add(value.toString(), name);
        return this;
    }

    public JsonEnvelopeBuilder withPayloadOf(final BigDecimal value, final String... name) {
        payload.add(value, name);
        return this;
    }

    public JsonEnvelopeBuilder withPayloadOf(final Integer value, final String... name) {
        payload.add(value, name);
        return this;
    }

    public JsonEnvelopeBuilder withPayloadOf(final Boolean value, final String... name) {
        payload.add(value, name);
        return this;
    }

    public JsonEnvelopeBuilder withPayloadOf(final String[] values, final String name) {
        final JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for (String value : values) {
            jsonArray.add(value);
        }
        payload.add(jsonArray, name);
        return this;
    }

    public JsonEnvelopeBuilder withPayloadOf(final JsonObject value, final String... name) {
        payload.add(value, name);
        return this;
    }

    public JsonEnvelopeBuilder withPayloadFrom(final JsonEnvelope envelope) {
        payload = new JsonObjectBuilderWrapper(envelope.payloadAsJsonObject());
        return this;
    }

    public JsonEnvelopeBuilder withNullPayload() {
        payload = null;
        return this;
    }

    public JsonEnvelope build() {
        return new DefaultJsonEnvelope(metadata != null ? metadata.build() : null, payload!=null ? payload.build() : NULL);
    }

    public String toJsonString() {
        return new JsonObjectEnvelopeConverter().fromEnvelope(build()).toString();
    }
}

