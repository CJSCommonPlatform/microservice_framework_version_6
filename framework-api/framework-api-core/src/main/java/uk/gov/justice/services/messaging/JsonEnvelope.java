package uk.gov.justice.services.messaging;

import uk.gov.justice.services.messaging.spi.JsonEnvelopeProvider;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * Interface for a messaging envelope containing metadata and a JsonValue payload.
 */
public interface JsonEnvelope extends Envelope<JsonValue> {

    String METADATA = "_metadata";

    /**
     * Provide an instance of a {@link JsonEnvelope} with given {@link Metadata} and {@link
     * JsonValue}.
     *
     * @param metadata the Metadata to be added to the JsonEnvelope
     * @param payload  the JsonValue to be added to the JsonEnvelope
     * @return the JsonEnvelope instance
     */
    static JsonEnvelope envelopeFrom(final Metadata metadata, final JsonValue payload) {
        return JsonEnvelopeProvider.provider().envelopeFrom(metadata, payload);
    }

    /**
     * Provide an instance of a {@link JsonEnvelope} with given {@link MetadataBuilder} and {@link
     * JsonValue}
     *
     * @param metadataBuilder the MetadataBuilder to be used to build {@link Metadata}
     * @param payload         the JsonValue to be added to the JsonEnvelope
     * @return the JsonEnvelope instance
     */
    static JsonEnvelope envelopeFrom(final MetadataBuilder metadataBuilder, final JsonValue payload) {
        return JsonEnvelopeProvider.provider().envelopeFrom(metadataBuilder, payload);
    }

    /**
     * Provide an instance of a {@link JsonEnvelope} with given {@link MetadataBuilder} and {@link
     * JsonObjectBuilder}
     *
     * @param metadataBuilder the MetadataBuilder to be used to build {@link Metadata}
     * @param payloadBuilder  the JsonObjectBuilder to be used to build {@link JsonValue}
     * @return the JsonEnvelope instance
     */
    static JsonEnvelope envelopeFrom(final MetadataBuilder metadataBuilder, final JsonObjectBuilder payloadBuilder) {
        return JsonEnvelopeProvider.provider().envelopeFrom(metadataBuilder, payloadBuilder);
    }

    /**
     * Provide an instance of a {@link MetadataBuilder}
     *
     * @return the MetadataBuilder instance
     */
    static MetadataBuilder metadataBuilder() {
        return JsonEnvelopeProvider.provider().metadataBuilder();
    }

    /**
     * Provide an instance of a {@link MetadataBuilder} from {@link Metadata}
     *
     * @param metadata the Metadata to add to the MetadataBuilder
     * @return the MetadataBuilder instance
     */
    static MetadataBuilder metadataFrom(final Metadata metadata) {
        return JsonEnvelopeProvider.provider().metadataFrom(metadata);
    }

    /**
     * Provide an instance of a {@link MetadataBuilder} from {@link JsonObject}
     *
     * @param jsonObject the JsonObject to add to the MetadataBuilder
     * @return the MetadataBuilder instance
     */
    static MetadataBuilder metadataFrom(final JsonObject jsonObject) {
        return JsonEnvelopeProvider.provider().metadataFrom(jsonObject);
    }

    JsonObject payloadAsJsonObject();

    JsonArray payloadAsJsonArray();

    JsonNumber payloadAsJsonNumber();

    JsonString payloadAsJsonString();

    JsonObject asJsonObject();

    /**
     * Pretty prints the actual json of this envelope.
     *
     * N.B. This should NEVER be used in production code as the json may contain sensitive data
     * which can be harmful should it appear in logs.
     *
     * If you want to log this envelope for tracing purposes then use the toString() method, as this
     * will give the objects metadata without exposing anything sensistive
     *
     * @return this envelope as it's json string, pretty printed.
     **/
    @Deprecated
    String toDebugStringPrettyPrint();


    /**
     * Prints the json for logging purposes. Removes any potentially sensitive data.
     *
     * @return a json String of the envelope
     */
    String toString();

    /**
     * Prints the json for debug purposes. Obfuscates any potentially sensitive data.
     *
     * @return a json String of the envelope with obfuscated payload values
     */
    String toObfuscatedDebugString();
}
