package uk.gov.justice.services.messaging;

import java.math.BigDecimal;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * Default implementation of an envelope.
 */
@Deprecated
public class DefaultJsonEnvelope implements JsonEnvelope {

    private JsonEnvelope jsonEnvelope;

    public DefaultJsonEnvelope(final Metadata metadata, final JsonValue payload) {
        jsonEnvelope = JsonEnvelope.envelopeFrom(metadata, payload);
    }

    /**
     * @param metadata the metadata to be added to the JsonEnvelope
     * @param payload  the JsonValue payload to be added to the JsonEnvelope
     * @return the JsonEnvelope
     * @deprecated Use the Enveloper for creating real envelopes in production code or the {@link
     * JsonEnvelope#envelopeFrom(Metadata, JsonValue)} for tests
     */
    @Deprecated
    public static JsonEnvelope envelopeFrom(final Metadata metadata, final JsonValue payload) {
        return JsonEnvelope.envelopeFrom(metadata, payload);
    }

    /**
     * @param metadataBuilder the metadata builder to create the metadata to be added to the
     *                        JsonEnvelope
     * @param payload         the JsonValue payload to be added to the JsonEnvelope
     * @return the JsonEnvelope
     * @deprecated Use the Enveloper for creating real envelopes in production code or the {@link
     * JsonEnvelope#envelopeFrom(MetadataBuilder, JsonValue)}
     */
    @Deprecated
    public static JsonEnvelope envelopeFrom(final uk.gov.justice.services.messaging.JsonObjectMetadata.Builder metadataBuilder, final JsonValue payload) {
        return envelopeFrom(metadataBuilder.build(), payload);
    }

    /**
     * @return the Builder
     * @deprecated Use the Enveloper for creating real envelopes in production code or the {@link
     * JsonEnvelope#envelopeFrom(MetadataBuilder, JsonObjectBuilder)}
     */
    @Deprecated
    public static Builder envelope() {
        return new Builder();
    }

    /**
     * @param envelope the JsonEnvelope that is added to the builder
     * @return the Builder
     * @deprecated Use the Enveloper for creating real envelopes in production code or the
     * JsonEnvelopeBuilder in test-utils for tests.
     */
    @Deprecated
    public static Builder envelopeFrom(final JsonEnvelope envelope) {
        return new Builder(envelope);
    }

    @Override
    public Metadata metadata() {
        return jsonEnvelope.metadata();
    }

    @Override
    public JsonValue payload() {
        return jsonEnvelope.payload();
    }

    @Override
    public JsonObject payloadAsJsonObject() {
        return jsonEnvelope.payloadAsJsonObject();
    }

    @Override
    public JsonArray payloadAsJsonArray() {
        return jsonEnvelope.payloadAsJsonArray();
    }

    @Override
    public JsonNumber payloadAsJsonNumber() {
        return jsonEnvelope.payloadAsJsonNumber();
    }

    @Override
    public JsonString payloadAsJsonString() {
        return jsonEnvelope.payloadAsJsonString();
    }

    @Override
    public JsonObject asJsonObject() {
        return jsonEnvelope.asJsonObject();
    }

    @Override
    public String toString() {
        return jsonEnvelope.toString();
    }

    @Override
    public String toDebugStringPrettyPrint() {
        return jsonEnvelope.toDebugStringPrettyPrint();
    }

    @Override
    public String toObfuscatedDebugString() {
        return jsonEnvelope.toObfuscatedDebugString();
    }

    /**
     * @deprecated Use the Enveloper for creating real envelopes in production code or the
     * JsonEnvelopeBuilder in test-utils for tests.
     */
    @Deprecated
    public static class Builder {

        private JsonObjectBuilderWrapper payload;
        private uk.gov.justice.services.messaging.JsonObjectMetadata.Builder metadata;

        private Builder() {
            payload = new JsonObjectBuilderWrapper();
        }

        public Builder(final JsonEnvelope envelope) {
            payload = new JsonObjectBuilderWrapper(envelope.payloadAsJsonObject());
            this.metadata = JsonObjectMetadata.metadataFrom(envelope.metadata());
        }

        public Builder with(final JsonObjectMetadata.Builder metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder withPayloadOf(final String value, final String... name) {
            payload.add(value, name);
            return this;
        }

        public Builder withPayloadOf(final UUID value, final String... name) {
            payload.add(value.toString(), name);
            return this;
        }

        public Builder withPayloadOf(final BigDecimal value, final String... name) {
            payload.add(value, name);
            return this;
        }

        public Builder withPayloadOf(final Integer value, final String... name) {
            payload.add(value, name);
            return this;
        }

        public Builder withPayloadOf(final Boolean value, final String... name) {
            payload.add(value, name);
            return this;
        }

        public Builder withPayloadOf(final String[] values, final String name) {
            final JsonArrayBuilder jsonArray = Json.createArrayBuilder();
            for (String value : values) {
                jsonArray.add(value);
            }
            payload.add(jsonArray, name);
            return this;
        }

        public Builder withPayloadOf(final JsonObject value, final String... name) {
            payload.add(value, name);
            return this;
        }

        public Builder withPayloadFrom(final JsonEnvelope envelope) {
            payload = new JsonObjectBuilderWrapper(envelope.payloadAsJsonObject());
            return this;
        }

        /**
         * @return the JsonEnvelope
         * @deprecated Use the Enveloper for creating real envelopes in production code or the
         * JsonEnvelopeBuilder in test-utils for tests.
         */
        @Deprecated
        public JsonEnvelope build() {
            return envelopeFrom(metadata != null ? metadata.build() : null, payload.build());
        }

        public String toJsonString() {
            return new DefaultJsonObjectEnvelopeConverter().fromEnvelope(build()).toString();
        }
    }
}
