package uk.gov.justice.services.messaging;

import java.math.BigDecimal;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
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

    public static Builder envelope() {
        return new Builder();
    }

    public static class Builder {
        private JsonObjectMetadata.Builder metadata;
        private final JsonObjectBuilderWrapper payload = new JsonObjectBuilderWrapper();

        private Builder() {
        }

        public Builder with(final JsonObjectMetadata.Builder metadata) {
            this.metadata = metadata;
            return this;
        }

        public JsonEnvelope build() {
            return envelopeFrom(metadata != null ? metadata.build() : null, payload.build());
        }

        public Builder withPayloadOf(final String value, final String... name) {
            payload.add(value, name);
            return this;
        }

        public Builder withPayloadOf(final BigDecimal value, final String... name) {
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
    }


}
