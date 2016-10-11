package uk.gov.justice.services.messaging;

import static uk.gov.justice.services.messaging.JsonObjectMetadata.CORRELATION;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.SESSION_ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.USER_ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.json.JSONObject;
import org.json.JSONTokener;

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

    public static JsonEnvelope envelopeFrom(final JsonObjectMetadata.Builder metadataBuilder, final JsonValue payload) {
        return envelopeFrom(metadataBuilder.build(), payload);
    }

    public static Builder envelope() {
        return new Builder();
    }

    public static Builder envelopeFrom(final JsonEnvelope envelope) {
        return new Builder(envelope);
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

    /**
     * Prints the json for logging purposes. Removes any potentially sensitive
     * data.
     *
     * @return a json String of the envelope
     */
    @Override
    public String toString() {
        final JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("id", String.valueOf(metadata.id()))
                .add("name", metadata.name());

        metadata.clientCorrelationId().ifPresent(s -> builder.add(CORRELATION, s));
        metadata.sessionId().ifPresent(s -> builder.add(SESSION_ID, s));
        metadata.userId().ifPresent(s -> builder.add(USER_ID, s));

        final JsonArrayBuilder causationBuilder = Json.createArrayBuilder();

        final List<UUID> causes = metadata.causation();

        if (causes != null) {
            metadata.causation().forEach(uuid -> causationBuilder.add(String.valueOf(uuid)));
        }
        return builder.add("causation", causationBuilder).build().toString();
    }

    /**
     * Returns a String of the JsonEnvelope as pretty printed json.
     *
     * Caution: the json envelope may contain sensitive data and so
     * this method should not be used for logging. Use toString()
     * for logging instead.
     *
     * @return the json envelope as pretty printed json
     */
    @Override
    public String toDebugStringPrettyPrint() {

        return new JSONObject(new JSONTokener(payload.toString()))
                .put("_metadata", new JSONObject(metadata.asJsonObject().toString()))
                .toString(2);
    }

    public static class Builder {
        private JsonObjectBuilderWrapper payload;
        private JsonObjectMetadata.Builder metadata;

        private Builder() {
            payload = new JsonObjectBuilderWrapper();
        }

        public Builder(final JsonEnvelope envelope) {
            payload = new JsonObjectBuilderWrapper(envelope.payloadAsJsonObject());
            this.metadata = metadataFrom(envelope.metadata());
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

        public JsonEnvelope build() {
            return envelopeFrom(metadata != null ? metadata.build() : null, payload.build());
        }

        public String toJsonString() {
            return new JsonObjectEnvelopeConverter().fromEnvelope(build()).toString();
        }
    }
}
