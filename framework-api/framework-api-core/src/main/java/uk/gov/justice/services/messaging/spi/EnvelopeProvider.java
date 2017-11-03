package uk.gov.justice.services.messaging.spi;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.MetadataBuilder;

import java.util.Iterator;
import java.util.ServiceLoader;

import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * Abstract class for EnvelopeProvider implementations to provide methods for constructing
 * {@link Envelope} and {@link MetadataBuilder} instances.
 *
 * Call the static method {@code EnvelopeProvider.provider()} to retrieve the
 * EnvelopeProvider instance from service dependency on the classpath.
 */
public interface EnvelopeProvider {

    /**
     * Loads an implementation of EnvelopeProvider using the {@link ServiceLoader} mechanism. An
     * instance of the first implementing class from the loader list is returned.
     *
     * @return an instance of EnvelopeProvider
     * @throws EnvelopeProviderNotFoundException if no implementations of EnvelopeProvider
     *                                               are found
     */
    public static EnvelopeProvider provider() {
        final ServiceLoader<EnvelopeProvider> loader = ServiceLoader.load(EnvelopeProvider.class);
        final Iterator<EnvelopeProvider> iterator = loader.iterator();

        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            throw new EnvelopeProviderNotFoundException("No EnvelopeProvider implementation found");
        }
    }

    /**
     * Provide an instance of a {@link Envelope} with given {@link Metadata} and {@link
     * JsonValue}.
     *
     * @param metadata the Metadata to be added to the Envelope
     * @param payload  the JsonValue to be added to the Envelope
     * @return the Envelope instance
     */
    public abstract <T> Envelope<T> envelopeFrom(final Metadata metadata, final T payload);

    /**
     * Provide an instance of a {@link Envelope} with given {@link MetadataBuilder} and {@link
     * JsonValue}
     *
     * @param metadataBuilder the MetadataBuilder to be used to build {@link Metadata}
     * @param payload         the JsonValue to be added to the Envelope
     * @return the Envelope instance
     */
    public abstract <T> Envelope<T> envelopeFrom(final MetadataBuilder metadataBuilder, final T payload);

    /**
     * Provide an instance of a {@link MetadataBuilder}
     *
     * @return the MetadataBuilder instance
     */
    public abstract MetadataBuilder metadataBuilder();

    /**
     * Provide an instance of a {@link MetadataBuilder} from the given {@link Metadata}
     *
     * @param metadata the Metadata to add to the MetadataBuilder
     * @return the MetadataBuilder instance
     */
    public abstract MetadataBuilder metadataFrom(final Metadata metadata);

    /**
     * Provide an instance of a {@link MetadataBuilder} from the given {@link JsonObject}
     *
     * @param jsonObject the JsonObject to add to the MetadataBuilder
     * @return the MetadataBuilder instance
     */
    public abstract MetadataBuilder metadataFrom(final JsonObject jsonObject);

}
