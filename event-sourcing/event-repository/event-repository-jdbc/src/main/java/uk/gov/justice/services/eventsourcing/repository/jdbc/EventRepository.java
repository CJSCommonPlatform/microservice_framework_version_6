package uk.gov.justice.services.eventsourcing.repository.jdbc;

import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

/**
 * Service to store and read event streams.
 */
public interface EventRepository {

    /**
     * Get a stream of event envelopes, ordered by position ascending.
     *
     * @return the stream of event envelopes. Never returns null.
     */
    Stream<JsonEnvelope> getEvents();

    /**
     * Get a stream of events ordered by ascending position.
     *
     * @param streamId the id of the stream to retrieve
     * @return the stream of envelopes. Never returns null.
     */
    Stream<JsonEnvelope> getEventsByStreamId(final UUID streamId);

    /**
     * Get a stream of event envelopes from a given position, ordered by position ascending.
     *
     * @param streamId   the id of the stream to retrieve
     * @param position the position to read the stream from
     * @return the stream of envelopes. Never returns null.
     */
    Stream<JsonEnvelope> getEventsByStreamIdFromPosition(final UUID streamId, final Long position);

    /**
     * Stores the given envelope into the event stream.
     *
     * @param envelope the envelope containing the event and the metadata.
     * @throws StoreEventRequestFailedException If there was a failure in storing the events, this
     *                                          will wrap the underlying cause.
     */
    void storeEvent(final JsonEnvelope envelope) throws StoreEventRequestFailedException;

    /**
     * Returns the position for the given stream id.
     *
     * @param streamId id of the stream.
     * @return position for the stream.  Returns 0 if stream doesn't exist. Never returns
     * null.
     */
    long getStreamSize(final UUID streamId);

    /**
     * Returns stream of envelope streams. Envelopes in the nested stream are ordered by position
     * ascending
     *
     * @return the stream of envelope streams
     */
    Stream<Stream<JsonEnvelope>> getStreamOfAllEventStreams();

    /**
     * Returns stream of all active event envelope stream. Envelopes in the nested stream are
     * ordered by position ascending
     *
     * @return the stream of active envelope streams
     */
    Stream<Stream<JsonEnvelope>> getStreamOfAllActiveEventStreams();

    /**
     * Returns Stream of all active streamIds
     *
     * @return the Stream of active streamIds
     */
    Stream<UUID> getAllActiveStreamIds();

    /**
     * Clears all of the events from a stream.
     *
     * @param id - the id of the stream that is to be Cleared.
     */
    void clearEventsForStream(final UUID id);

    /**
     * Get a stream of EventStream, ordered by position.
     *
     * @param position the position of the stream to retrieve
     * @return the stream of EventStreamObject. Never returns null.
     */
    Stream<EventStreamMetadata> getEventStreamsFromPosition(final long position);

    /**
     * Mark the stream as active or inactive.
     *
     * @param streamId the streamId of the stream to mark active.
     * @param active   indicates if the stream is active.
     */
    void markEventStreamActive(final UUID streamId, final boolean active);

    /**
     * Creates an event stream record with automatic position.
     *
     * @param streamId the streamId of the stream to save.
     */
    void createEventStream(final UUID streamId);


    /**
     * Returns the stream position.
     *
     * @return the latest stream position in the event streams.
     */
    long getStreamPosition(final UUID streamId);

    /**
     * Returns stream of EventStreamMetadata
     * ascending
     *
     * @return the stream of envelope streams
     */
    Stream<EventStreamMetadata> getStreams();

}
