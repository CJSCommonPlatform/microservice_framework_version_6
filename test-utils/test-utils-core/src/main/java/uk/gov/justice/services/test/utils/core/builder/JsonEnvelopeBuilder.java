package uk.gov.justice.services.test.utils.core.builder;


import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Map;
import java.util.UUID;

import javax.json.JsonObjectBuilder;

public class JsonEnvelopeBuilder {

    private JsonObjectBuilder metadataJson = createObjectBuilder();
    private JsonObjectBuilder payloadJson = createObjectBuilder();

    public static JsonEnvelopeBuilder envelope() {
        return new JsonEnvelopeBuilder();
    }

    public static JsonEnvelopeBuilder envelopeWithDefaultMetadata() {
        return envelope().withMetadataOf("id", UUID.randomUUID().toString(), "name", "defaultName");

    }

    public JsonEnvelopeBuilder withPayloadOf(final String key, final String value) {
        this.payloadJson.add(key, value);
        return this;
    }

    public JsonEnvelopeBuilder withPayloadOf(final String key, final int value) {
        this.payloadJson.add(key, value);
        return this;
    }

    public JsonEnvelopeBuilder withPayloadOf(final String key, final double value) {
        this.payloadJson.add(key, value);
        return this;
    }

    public JsonEnvelopeBuilder withPayloadOf(final String key, final boolean value) {
        this.payloadJson.add(key, value);
        return this;
    }

    public JsonEnvelopeBuilder withPayloadOf(final String key1, final String value1, final String key2, final String value2) {
        this.payloadJson.add(key1, value1).add(key2, value2);
        return this;
    }

    public JsonEnvelopeBuilder withPayloadOf(final String key1, final String value1, final String key2, final String value2, final String key3, final String value3) {
        this.payloadJson.add(key1, value1).add(key2, value2).add(key3, value3);
        return this;
    }


    public JsonEnvelopeBuilder withMetadataOf(final String key, final String value) {
        this.metadataJson.add(key, value);
        return this;
    }

    public JsonEnvelopeBuilder withMetadataOf(final String key1, final String value1, final String key2, final String value2) {
        this.metadataJson.add(key1, value1).add(key2, value2);
        return this;
    }

    public JsonEnvelopeBuilder withMetadataOf(final String key1, final String value1, final String key2, final String value2, final String key3, final String value3) {
        this.metadataJson.add(key1, value1).add(key2, value2).add(key3, value3);
        return this;
    }

    public JsonEnvelopeBuilder withMetadataOf(final Map<String, String> metadata) {
        for (final String meta : metadata.keySet()) {
            this.metadataJson.add(meta, metadata.get(meta));
        }
        return this;
    }

    public JsonEnvelope build() {
        return envelopeFrom(metadataFrom(metadataJson.build()), payloadJson.build());
    }

}
