package uk.gov.justice.services.test.utils.core.messaging;

import static javax.json.JsonValue.NULL;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.MetadataBuilder;
import uk.gov.justice.services.test.utils.core.messaging.JsonObjectBuilderWrapper;

import java.math.BigDecimal;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 * Implementation of the {@link JsonEnvelope} specifically for testing purposes that include useful
 * builder options. For production code the Enveloper should be used to create envelopes.
 */
public class JsonEnvelopeBuilder {

    private JsonObjectBuilderWrapper payload;
    private MetadataBuilder metadata;

    /**
     * @param metadata the metadata
     * @param payload  the payload
     * @return a JsonEnvelope
     * @deprecated Use {@link JsonEnvelope#envelopeFrom(Metadata, JsonValue)}
     */
    @Deprecated
    public static JsonEnvelope envelopeFrom(final Metadata metadata, final JsonValue payload) {
        return JsonEnvelope.envelopeFrom(metadata, payload);
    }

    /**
     * @param metadataBuilder the metadataBuilder
     * @param payload         the payload
     * @return a JsonEnvelope
     * @deprecated Use {@link JsonEnvelope#envelopeFrom(MetadataBuilder, JsonValue)}
     */
    @Deprecated
    public static JsonEnvelope envelopeFrom(final JsonObjectMetadata.Builder metadataBuilder, final JsonValue payload) {
        return JsonEnvelope.envelopeFrom(metadataBuilder.build(), payload);
    }

    /**
     * @param metadataBuilder the metadataBuilder
     * @param payloadBuilder  the payloadBuilder
     * @return a JsonEnvelope
     * @deprecated Use {@link JsonEnvelope#envelopeFrom(MetadataBuilder, JsonObjectBuilder)}
     */
    @Deprecated
    public static JsonEnvelope envelopeFrom(final JsonObjectMetadata.Builder metadataBuilder, final JsonObjectBuilder payloadBuilder) {
        return JsonEnvelope.envelopeFrom(metadataBuilder.build(), payloadBuilder.build());
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

    public JsonEnvelopeBuilder with(final MetadataBuilder metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * @deprecated Use {@link JsonEnvelopeBuilder#with(MetadataBuilder)}
     */
    @Deprecated
    public JsonEnvelopeBuilder with(final JsonObjectMetadata.Builder metadata) {
        this.metadata = metadataFrom(metadata.build());
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

    public JsonEnvelopeBuilder withPayloadOf(final JsonArray value, final String... name) {
        payload.add(value, name);
        return this;
    }

    /**
     * Convenience method to create the envelope payload from the given {@link JsonObject} as root
     * element.  This is a destructive operations and will undo all earlier builder operations.  It
     * will continue to support additional builder operations
     *
     * @param rootObject the complete constructed payload to use
     */
    public JsonEnvelopeBuilder withPayloadFrom(final JsonObject rootObject) {
        payload = new JsonObjectBuilderWrapper(rootObject);
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
        return JsonEnvelope.envelopeFrom(metadata != null ? metadata.build() : null, payload != null ? payload.build() : NULL);
    }

    public String toJsonString() {
        return new DefaultJsonObjectEnvelopeConverter().fromEnvelope(build()).toString();
    }
}

