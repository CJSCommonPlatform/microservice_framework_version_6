package uk.gov.justice.services.test.utils.core.messaging;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.MetadataBuilder;

import java.util.UUID;

import javax.json.JsonObject;

public final class MetadataBuilderFactory {

    private MetadataBuilderFactory() {
    }

    /**
     * Create metadata builder
     *
     * @param id   the metadata UUID
     * @param name the metadata name
     * @return metadata builder
     */
    public static MetadataBuilder metadataOf(final UUID id, final String name) {
        return metadataBuilder().withId(id).withName(name);
    }

    /**
     * Create metadata builder
     *
     * @param id   the metadata UUID
     * @param name the metadata name
     * @return metadata builder
     */
    public static MetadataBuilder metadataOf(final String id, final String name) {
        return metadataOf(UUID.fromString(id), name);
    }

    /**
     * Create metadata builder with random id
     *
     * @param name the metadata name
     * @return metadata builder
     */
    public static MetadataBuilder metadataWithRandomUUID(final String name) {
        return metadataOf(randomUUID(), name);
    }

    /**
     * Create metadata builder with random id and dummy name
     *
     * @return metadata builder
     */
    public static MetadataBuilder metadataWithRandomUUIDAndName() {
        return metadataWithRandomUUID("dummy");
    }

    /**
     * Create metadata builder with random id and dummy name To be used in unit tests
     *
     * @return metadata builder
     */
    public static MetadataBuilder metadataWithDefaults() {
        return metadataWithRandomUUIDAndName().createdAt(now());
    }

    /**
     * Create a {@link MetadataBuilder} object from a {@link JsonObject}.
     *
     * @param metadata the {@link Metadata} to build the metadata from
     * @return the {@link MetadataBuilder}
     */
    public static MetadataBuilder metadataFrom(final Metadata metadata) {
        return JsonEnvelope.metadataFrom(metadata);
    }

    /**
     * Create a {@link MetadataBuilder} object from a {@link JsonObject}.
     *
     * @param jsonObject the {@link JsonObject} to build the metadata from
     * @return the {@link MetadataBuilder}
     */
    public static MetadataBuilder metadataFrom(final JsonObject jsonObject) {
        return JsonEnvelope.metadataFrom(jsonObject);
    }
}
