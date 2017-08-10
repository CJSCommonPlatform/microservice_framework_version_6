package uk.gov.justice.services.messaging;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Interface defining a MetadataBuilder that builds a {@link Metadata} instance from the given
 * data.
 */
public interface MetadataBuilder {

    /**
     * Add a id to the metadata
     *
     * @param id unique id used to definitively identify the payload within the system
     * @return the current instance of the MetadataBuilder
     */
    MetadataBuilder withId(final UUID id);

    /**
     * Add a date created to the metadata
     *
     * @param dateCreated timestamp for when the metadata was created
     * @return the current instance of the MetadataBuilder
     */
    MetadataBuilder createdAt(final ZonedDateTime dateCreated);

    /**
     * Add a name to the metadata
     *
     * @param name logical type name of the message payload
     * @return the current instance of the MetadataBuilder
     */
    MetadataBuilder withName(final String name);

    /**
     * Add a causation ids to the metadata
     *
     * @param causation ids that indicate the sequence of commands or events that resulting in this
     *                  message
     * @return the current instance of the MetadataBuilder
     */
    MetadataBuilder withCausation(final UUID... causation);

    /**
     * Add a client id to the metadata
     *
     * @param clientId correlation id supplied by the client
     * @return the current instance of the MetadataBuilder
     */
    MetadataBuilder withClientCorrelationId(final String clientId);

    /**
     * Add a user id to the metadata
     *
     * @param userId id of the user that initiated this message
     * @return the current instance of the MetadataBuilder
     */
    MetadataBuilder withUserId(final String userId);

    /**
     * Add a session id to the metadata
     *
     * @param sessionId id of the user's session that initiated this message
     * @return the current instance of the MetadataBuilder
     */
    MetadataBuilder withSessionId(final String sessionId);

    /**
     * Add a stream id to the metadata
     *
     * @param streamId UUID of the stream this message belongs to
     * @return the current instance of the MetadataBuilder
     */
    MetadataBuilder withStreamId(final UUID streamId);

    /**
     * Add a version to the metadata
     *
     * @param version sequence id (or version) that indicates where in the stream this message is
     *                positioned
     * @return the current instance of the MetadataBuilder
     */
    MetadataBuilder withVersion(final long version);

    /**
     * Build a {@link Metadata} instance from the added data.
     *
     * @return the {@link Metadata} instance
     */
    Metadata build();
}
