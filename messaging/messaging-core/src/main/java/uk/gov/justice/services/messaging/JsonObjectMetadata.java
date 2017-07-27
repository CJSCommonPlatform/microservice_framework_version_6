package uk.gov.justice.services.messaging;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

/**
 * @deprecated Use static methods of {@link JsonEnvelope} in production code and
 * MetadataBuilderFactory in test-utils for tests.
 */
@Deprecated
public class JsonObjectMetadata implements Metadata {

    /**
     * @deprecated Use {@link JsonMetadata#ID}
     */
    @Deprecated
    public static final String ID = "id";
    /**
     * @deprecated Use {@link JsonMetadata#NAME}
     */
    @Deprecated
    public static final String NAME = "name";
    /**
     * @deprecated Use {@link JsonMetadata#CREATED_AT}
     */
    @Deprecated
    public static final String CREATED_AT = "createdAt";
    /**
     * @deprecated Use {@link JsonMetadata#CORRELATION}
     */
    @Deprecated
    public static final String CORRELATION = "correlation";
    /**
     * @deprecated Use {@link JsonMetadata#CLIENT_ID}
     */
    @Deprecated
    public static final String CLIENT_ID = "client";
    /**
     * @deprecated Use {@link JsonMetadata#CONTEXT}
     */
    @Deprecated
    public static final String CONTEXT = "context";
    /**
     * @deprecated Use {@link JsonMetadata#USER_ID}
     */
    @Deprecated
    public static final String USER_ID = "user";
    /**
     * @deprecated Use {@link JsonMetadata#SESSION_ID}
     */
    @Deprecated
    public static final String SESSION_ID = "session";
    /**
     * @deprecated Use {@link JsonMetadata#STREAM}
     */
    @Deprecated
    public static final String STREAM = "stream";
    /**
     * @deprecated Use {@link JsonMetadata#STREAM_ID}
     */
    @Deprecated
    public static final String STREAM_ID = "id";
    /**
     * @deprecated Use {@link JsonMetadata#VERSION}
     */
    @Deprecated
    public static final String VERSION = "version";
    /**
     * @deprecated Use {@link JsonMetadata#CAUSATION}
     */
    @Deprecated
    public static final String CAUSATION = "causation";

    private final Metadata metadata;

    private JsonObjectMetadata(final Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * @param jsonObject the {@link JsonObject} to build the metadata from
     * @return the {@link Metadata}
     * @deprecated Use {@link JsonEnvelope#metadataFrom(JsonObject)} in production code and
     * MetadataBuilderFactory in test-utils for tests.
     */
    @Deprecated
    public static Metadata metadataFrom(final JsonObject jsonObject) {
        return new JsonObjectMetadata(JsonEnvelope.metadataFrom(jsonObject).build());
    }

    /**
     * @param id   the metadata UUID
     * @param name the metadata name
     * @return metadata builder
     * @deprecated Use {@link JsonEnvelope#metadataBuilder()} in production code and
     * MetadataBuilderFactory in test-utils for tests.
     */
    @Deprecated
    public static Builder metadataOf(final UUID id, final String name) {
        return new Builder().withId(id).withName(name);
    }

    /**
     * @param id   the metadata UUID
     * @param name the metadata name
     * @return metadata builder
     * @deprecated Use {@link JsonEnvelope#metadataBuilder()} in production code and
     * MetadataBuilderFactory in test-utils for tests.
     */
    @Deprecated
    public static Builder metadataOf(final String id, final String name) {
        return metadataOf(UUID.fromString(id), name);
    }

    /**
     * @param name the metadata name
     * @return metadata builder
     * @deprecated Use {@link JsonEnvelope#metadataBuilder()} in production code and
     * MetadataBuilderFactory in test-utils for tests.
     */
    @Deprecated
    public static Builder metadataWithRandomUUID(final String name) {
        return metadataOf(randomUUID(), name);
    }

    /**
     * @return metadata builder
     * @deprecated Use {@link JsonEnvelope#metadataBuilder()} in production code and
     * MetadataBuilderFactory in test-utils for tests.
     */
    @Deprecated
    public static Builder metadataWithRandomUUIDAndName() {
        return metadataWithRandomUUID("dummy");
    }

    /**
     * @return metadata builder
     * @deprecated Use {@link JsonEnvelope#metadataBuilder()} in production code and
     * MetadataBuilderFactory in test-utils for tests.
     */
    @Deprecated
    public static Builder metadataWithDefaults() {
        return metadataWithRandomUUIDAndName().createdAt(now());
    }

    /**
     * @param metadata the {@link Metadata} to build the metadata from
     * @return the {@link Builder}
     * @deprecated Use {@link JsonEnvelope#metadataFrom(JsonObject)} in production code and
     * MetadataBuilderFactory in test-utils for tests.
     */
    @Deprecated
    public static Builder metadataFrom(final Metadata metadata) {
        return new Builder(metadata);
    }


    @Override
    public UUID id() {
        return metadata.id();
    }

    @Override
    public String name() {
        return metadata.name();
    }

    @Override
    public Optional<String> clientCorrelationId() {
        return metadata.clientCorrelationId();
    }

    @Override
    public List<UUID> causation() {
        return metadata.causation();
    }

    @Override
    public Optional<String> userId() {
        return metadata.userId();
    }

    @Override
    public Optional<String> sessionId() {
        return metadata.sessionId();
    }

    @Override
    public Optional<UUID> streamId() {
        return metadata.streamId();
    }

    @Override
    public Optional<Long> version() {
        return metadata.version();
    }

    @Override
    public JsonObject asJsonObject() {
        return metadata.asJsonObject();
    }

    @Override
    public Optional<ZonedDateTime> createdAt() {
        return metadata.createdAt();
    }

    @Override
    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonObjectMetadata that = (JsonObjectMetadata) o;
        return Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata);
    }

    public static class Builder {

        private final MetadataBuilder metadataBuilder;

        private Builder() {
            metadataBuilder = JsonEnvelope.metadataBuilder();
        }

        private Builder(final Metadata metadata) {
            metadataBuilder = JsonEnvelope.metadataFrom(metadata);
        }

        /**
         * @param id unique id used to definitively identify the payload within the system
         * @return metadata builder
         */
        public Builder withId(final UUID id) {
            metadataBuilder.withId(id);
            return this;
        }

        /**
         * @param dateCreated timestamp for when the metadata was created
         * @return metadata builder
         */
        public Builder createdAt(final ZonedDateTime dateCreated) {
            metadataBuilder.createdAt(dateCreated);
            return this;
        }

        /**
         * @param name logical type name of the message payload
         * @return metadata builder
         */
        public Builder withName(final String name) {
            metadataBuilder.withName(name);
            return this;
        }

        /**
         * @param uuid ids that indicate the sequence of commands or events that resulting in this
         *             message
         * @return metadata builder
         */
        public Builder withCausation(final UUID... uuid) {
            metadataBuilder.withCausation(uuid);
            return this;
        }

        /**
         * @param clientId correlation id supplied by the client
         * @return metadata builder
         */
        public Builder withClientCorrelationId(final String clientId) {
            metadataBuilder.withClientCorrelationId(clientId);
            return this;
        }

        /**
         * @param userId id of the user that initiated this message
         * @return metadata builder
         */
        public Builder withUserId(final String userId) {
            metadataBuilder.withUserId(userId);
            return this;
        }

        /**
         * @param sessionId id of the user's session that initiated this message
         * @return metadata builder
         */
        public Builder withSessionId(final String sessionId) {
            metadataBuilder.withSessionId(sessionId);
            return this;
        }

        /**
         * @param streamId UUID of the stream this message belongs to
         * @return metadata builder
         */
        public Builder withStreamId(final UUID streamId) {
            metadataBuilder.withStreamId(streamId);
            return this;
        }

        /**
         * @param version sequence id (or version) that indicates where in the stream this message
         *                is positioned
         * @return metadata builder
         */
        public Builder withVersion(final long version) {
            metadataBuilder.withVersion(version);
            return this;
        }

        public Metadata build() {
            return metadataBuilder.build();
        }
    }
}
