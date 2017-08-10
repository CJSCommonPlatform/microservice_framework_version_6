package uk.gov.justice.services.messaging.spi;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.MetadataBuilder;

import java.util.Iterator;
import java.util.ServiceLoader;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 * Abstract class for JsonEnvelopeProvider implementations to provide methods for constructing
 * {@link JsonEnvelope} and {@link MetadataBuilder} instances.
 *
 * Call the static method {@code JsonEnvelopeProvider.provider()} to retrieve the
 * JsonEnvelopeProvider instance from service dependency on the classpath.
 */
public abstract class JsonEnvelopeProvider {

    /**
     * Loads an implementation of JsonEnvelopeProvider using the {@link ServiceLoader} mechanism. An
     * instance of the first implementing class from the loader list is returned.
     *
     * @return an instance of JsonEnvelopeProvider
     * @throws JsonEnvelopeProviderNotFoundException if no implementations of JsonEnvelopeProvider
     *                                               are found
     */
    public static JsonEnvelopeProvider provider() {
        final ServiceLoader<JsonEnvelopeProvider> loader = ServiceLoader.load(JsonEnvelopeProvider.class);
        final Iterator<JsonEnvelopeProvider> iterator = loader.iterator();

        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            throw new JsonEnvelopeProviderNotFoundException("No JsonEnvelopeProvider implementation found");
        }
    }

    /**
     * Provide an instance of a {@link JsonEnvelope} with given {@link Metadata} and {@link
     * JsonValue}.
     *
     * @param metadata the Metadata to be added to the JsonEnvelope
     * @param payload  the JsonValue to be added to the JsonEnvelope
     * @return the JsonEnvelope instance
     */
    public abstract JsonEnvelope envelopeFrom(final Metadata metadata, final JsonValue payload);

    /**
     * Provide an instance of a {@link JsonEnvelope} with given {@link MetadataBuilder} and {@link
     * JsonValue}
     *
     * @param metadataBuilder the MetadataBuilder to be used to build {@link Metadata}
     * @param payload         the JsonValue to be added to the JsonEnvelope
     * @return the JsonEnvelope instance
     */
    public abstract JsonEnvelope envelopeFrom(final MetadataBuilder metadataBuilder, final JsonValue payload);

    /**
     * Provide an instance of a {@link JsonEnvelope} with given {@link MetadataBuilder} and {@link
     * JsonObjectBuilder}
     *
     * @param metadataBuilder the MetadataBuilder to be used to build {@link Metadata}
     * @param payloadBuilder  the JsonObjectBuilder to be used to build {@link JsonValue}
     * @return the JsonEnvelope instance
     */
    public abstract JsonEnvelope envelopeFrom(final MetadataBuilder metadataBuilder, final JsonObjectBuilder payloadBuilder);

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
