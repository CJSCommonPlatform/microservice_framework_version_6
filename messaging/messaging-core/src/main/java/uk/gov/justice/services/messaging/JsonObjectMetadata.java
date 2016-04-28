package uk.gov.justice.services.messaging;

import static uk.gov.justice.services.messaging.JsonObjects.getJsonString;
import static uk.gov.justice.services.messaging.JsonObjects.getLong;
import static uk.gov.justice.services.messaging.JsonObjects.getString;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;
import static uk.gov.justice.services.messaging.JsonObjects.getUUIDs;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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

    private static final String[] USER_ID_PATH = new String[]{CONTEXT, USER_ID};
    private static final String[] CLIENT_CORRELATION_PATH = new String[]{CORRELATION, CLIENT_ID};
    private static final String[] VERSION_PATH = new String[]{STREAM, VERSION};
    private static final String[] SESSION_ID_PATH = new String[]{CONTEXT, SESSION_ID};
    private static final String[] STREAM_ID_PATH = new String[]{STREAM, STREAM_ID};

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
}
