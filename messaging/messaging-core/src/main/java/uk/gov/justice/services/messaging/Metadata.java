package uk.gov.justice.services.messaging;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

/**
 * Interface for an envelope's metadata.
 */
public interface Metadata {

    /**
     * A system generated unique id used to definitively identify the payload within the system.
     *
     * @return the id
     */
    UUID id();

    /**
     * Get the logical type name of the message payload.
     *
     * @return the name
     */
    String name();

    /**
     * Get the correlation id supplied by the client, if one was specified
     *
     * @return the client's correlation id
     */
    Optional<String> clientCorrelationId();

    /**
     * Get a list of ids that indicate the sequence of commands or events that resulting in this
     * message
     *
     * @return the causation list
     */
    List<UUID> causation();

    /**
     * Get the id of the user that initiated this message if one was specified.
     *
     * @return the user id
     */
    Optional<String> userId();

    /**
     * Get the id of the user's session that initiated this message if one was specified.
     *
     * @return the session id
     */
    Optional<String> sessionId();

    /**
     * Get the UUID of the stream this message belongs to, if one is specified.
     *
     * @return the optional UUID
     */
    Optional<UUID> streamId();

    /**
     * Get the sequence id (or version) that indicates where in the stream this message is
     * positioned, if one is specified.
     *
     * @return the optional sequence id
     */
    Optional<Long> version();

    /**
     * Return the whole metadata as a JsonObject.
     *
     * @return the metadata
     */
    JsonObject asJsonObject();

    /**
     * Return the timestamp for when the metadata was created
     *
     * @return the timestamp the matadata was created at
     */
    Optional<ZonedDateTime> createdAt();
}
