package uk.gov.justice.services.messaging;

import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonString;
import static uk.gov.justice.services.messaging.JsonObjects.getLong;
import static uk.gov.justice.services.messaging.JsonObjects.getString;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;
import static uk.gov.justice.services.messaging.JsonObjects.getUUIDs;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonString;

/**
 * Implementation of metadata that uses a JsonObject internally to store the metadata.
 */
public class JsonObjectMetadata implements Metadata {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String CORRELATION = "correlation";
    public static final String CLIENT_ID = "client";
    public static final String CONTEXT = "context";
    public static final String USER_ID = "user";
    public static final String SESSION_ID = "session";
    public static final String STREAM = "stream";
    public static final String STREAM_ID = "id";
    public static final String VERSION = "version";
    public static final String CAUSATION = "causation";

    private static final String[] USER_ID_PATH = {CONTEXT, USER_ID};
    private static final String[] CLIENT_CORRELATION_PATH = {CORRELATION, CLIENT_ID};
    private static final String[] VERSION_PATH = {STREAM, VERSION};
    private static final String[] SESSION_ID_PATH = {CONTEXT, SESSION_ID};
    private static final String[] STREAM_ID_PATH = {STREAM, STREAM_ID};

    private final JsonObject metadata;

    private JsonObjectMetadata(final JsonObject metadata) {
        this.metadata = metadata;
    }

    /**
     * Instantiate a {@link JsonObjectMetadata} object from a {@link JsonObject}.
     *
     * @param jsonObject the {@link JsonObject} to build the metadata from
     * @return the {@link JsonObjectMetadata}
     */
    public static Metadata metadataFrom(final JsonObject jsonObject) {

        JsonString id = getJsonString(jsonObject, ID)
                .orElseThrow(() -> new IllegalArgumentException("Missing id field"));
        UUID.fromString(id.getString());

        JsonString name = getJsonString(jsonObject, NAME)
                .orElseThrow(() -> new IllegalArgumentException("Missing name field"));
        if (name.getString().isEmpty()) {
            throw new IllegalArgumentException("Name field cannot be empty");
        }

        return new JsonObjectMetadata(jsonObject);
    }

    /**
     * Create metadata builder
     *
     * @return metadata builder
     */
    public static Builder metadataOf(final UUID id, final String name) {
        return new Builder().withId(id).withName(name);
    }

    /**
     * Create metadata builder with random id
     *
     * @return metadata builder
     */
    public static Builder metadataWithRandomUUID(final String name) {
        return metadataOf(randomUUID(), name);
    }

    /**
     * Create metadata builder with random id and dummy name
     * To be used in unit tests
     *
     * @return metadata builder
     */
    public static Builder metadataWithDefaults() {
        return metadataOf(randomUUID(), "dummyName");
    }

    @Override
    public UUID id() {
        return getUUID(metadata, ID)
                .orElseThrow(() -> new IllegalStateException("Missing id field"));
    }

    @Override
    public String name() {
        return getString(metadata, NAME)
                .orElseThrow(() -> new IllegalStateException("Missing name field"));
    }

    @Override
    public Optional<String> clientCorrelationId() {
        return getString(metadata, CLIENT_CORRELATION_PATH);
    }

    @Override
    public List<UUID> causation() {
        return getUUIDs(metadata, CAUSATION);
    }

    @Override
    public Optional<String> userId() {
        return getString(metadata, USER_ID_PATH);
    }

    @Override
    public Optional<String> sessionId() {
        return getString(metadata, SESSION_ID_PATH);
    }

    @Override
    public Optional<UUID> streamId() {
        return getUUID(metadata, STREAM_ID_PATH);
    }

    @Override
    public Optional<Long> version() {
        return getLong(metadata, VERSION_PATH);
    }

    @Override
    public JsonObject asJsonObject() {
        return metadata;
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

        private JsonObjectBuilderWrapper json = new JsonObjectBuilderWrapper();

        private Builder() {
        }

        /**
         * @param id unique id used to definitively identify the payload within the system
         * @return metadata builder
         */
        public Builder withId(final UUID id) {
            json.add(id.toString(), ID);
            return this;
        }

        /**
         * @param name logical type name of the message payload
         * @return metadata builder
         */
        public Builder withName(final String name) {
            json.add(name, NAME);
            return this;
        }

        /**
         * @param uuid ids that indicate the sequence of commands or events that resulting in this
         * message
         * @return metadata builder
         */
        public Builder withCausation(final UUID... uuid) {
            final JsonArrayBuilder causationArray = Json.createArrayBuilder();
            for (UUID id : uuid) {
                causationArray.add(id.toString());
            }
            json.add(causationArray, CAUSATION);
            return this;
        }

        /**
         * @param clientId correlation id supplied by the client
         * @return metadata builder
         */
        public Builder withClientCorrelationId(final String clientId) {
            json.add(clientId, CLIENT_CORRELATION_PATH);
            return this;
        }

        /**
         * @param userId id of the user that initiated this message
         * @return metadata builder
         */
        public Builder withUserId(final String userId) {
            json.add(userId, USER_ID_PATH);
            return this;
        }

        /**
         * @param sessionId id of the user's session that initiated this message
         * @return metadata builder
         */
        public Builder withSessionId(final String sessionId) {
            json.add(sessionId, SESSION_ID_PATH);
            return this;
        }

        /**
         * @param streamId UUID of the stream this message belongs to
         * @return metadata builder
         */
        public Builder withStreamId(final UUID streamId) {
            json.add(streamId.toString(), STREAM_ID_PATH);
            return this;
        }
        /**
         * @param version sequence id (or version) that indicates where in the stream this message is
         * positioned
         * @return metadata builder
         */
        public Builder withVersion(final Long version) {
            json.add(BigDecimal.valueOf(version), VERSION_PATH);
            return this;
        }

        public Metadata build() {
            return metadataFrom(json.build());
        }


    }
}
