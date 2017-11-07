package uk.gov.justice.services.messaging;

import uk.gov.justice.services.messaging.spi.EnvelopeProvider;

import javax.json.JsonObject;

/**
 * Interface for a messaging envelope containing metadata and a payload.
 *
 * @param <T> the type of payload this envelope can contain
 */
public interface Envelope<T> {

    Metadata metadata();

    T payload();


    /**
     * Provide an instance of a {@link Envelope} with given {@link Metadata} and payload.
     *
     * @param metadata the Metadata to be added to the Envelope
     * @param payload  the Payload to be added to the Envelope
     * @return the Envelope instance
     */
    static <T> Envelope<T> envelopeFrom(final Metadata metadata, final T payload) {
        return EnvelopeProvider.provider().envelopeFrom(metadata, payload);
    }

    /**
     * Provide an instance of a {@link Envelope} with given {@link MetadataBuilder} and payload.
     *
     * @param metadataBuilder the MetadataBuilder to be used to build {@link Metadata}
     * @param payload         the Payload to be added to the Envelope
     * @return the Envelope instance
     */
    static <T> Envelope<T> envelopeFrom(final MetadataBuilder metadataBuilder, final T payload) {
        return EnvelopeProvider.provider().envelopeFrom(metadataBuilder, payload);
    }

    /**
     * Provide an instance of a {@link MetadataBuilder}
     *
     * @return the MetadataBuilder instance
     */
    static MetadataBuilder metadataBuilder() {
        return EnvelopeProvider.provider().metadataBuilder();
    }

    /**
     * Provide an instance of a {@link MetadataBuilder} from {@link Metadata}
     *
     * @param metadata the Metadata to add to the MetadataBuilder
     * @return the MetadataBuilder instance
     */
    static MetadataBuilder metadataFrom(final Metadata metadata) {
        return EnvelopeProvider.provider().metadataFrom(metadata);
    }

    /**
     * Provide an instance of a {@link MetadataBuilder} from {@link JsonObject}
     *
     * @param jsonObject the JsonObject to add to the MetadataBuilder
     * @return the MetadataBuilder instance
     */
    static MetadataBuilder metadataFrom(final JsonObject jsonObject) {
        return EnvelopeProvider.provider().metadataFrom(jsonObject);
    }

}
