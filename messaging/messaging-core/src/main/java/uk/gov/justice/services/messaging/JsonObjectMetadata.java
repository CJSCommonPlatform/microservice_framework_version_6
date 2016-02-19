package uk.gov.justice.services.messaging;

import javax.json.JsonObject;
import javax.json.JsonString;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.justice.services.messaging.JsonObjects.getJsonString;
import static uk.gov.justice.services.messaging.JsonObjects.getLong;
import static uk.gov.justice.services.messaging.JsonObjects.getString;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;
import static uk.gov.justice.services.messaging.JsonObjects.getUUIDs;

/**
 * Implementation of metadata that uses a JsonObject internally to store the metadata.
 */
public class JsonObjectMetadata implements Metadata {

    private final JsonObject metadata;

    private JsonObjectMetadata(final JsonObject metadata) {
        this.metadata = metadata;
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
        return getString(metadata, CLIENT_CORRELATION);
    }

    @Override
    public List<UUID> causation() {
        return getUUIDs(metadata, CAUSATION);
    }

    @Override
    public Optional<String> userId() {
        return getString(metadata, USER_ID);
    }

    @Override
    public Optional<String> sessionId() {
        return getString(metadata, SESSION_ID);
    }

    @Override
    public Optional<UUID> streamId() {
        return getUUID(metadata, STREAM_ID);
    }

    @Override
    public Optional<Long> version() {
        return getLong(metadata, VERSION);
    }

    @Override
    public JsonObject asJsonObject() {
        return metadata;
    }

    /**
     * Instantiate a {@link JsonObjectMetadata} object from a {@link JsonObject}.
     *
     * @param jsonObject the {@link JsonObject} to build the metadata from
     * @return the {@link JsonObjectMetadata}
     */
    public static Metadata metadataFrom(final JsonObject jsonObject) {

        JsonString id = getJsonString(jsonObject, Metadata.ID)
                .orElseThrow(() -> new IllegalArgumentException("Missing id field"));
        UUID.fromString(id.getString());

        JsonString name = getJsonString(jsonObject, Metadata.NAME)
                .orElseThrow(() -> new IllegalArgumentException("Missing name field"));
        if (name.getString().isEmpty()) {
            throw new IllegalArgumentException("Name field cannot be empty");
        }

        return new JsonObjectMetadata(jsonObject);
    }
}
