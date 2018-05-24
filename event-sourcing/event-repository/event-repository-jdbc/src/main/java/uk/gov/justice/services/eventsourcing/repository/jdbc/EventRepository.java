package uk.gov.justice.services.eventsourcing.repository.jdbc;


import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.StoreEventRequestFailedException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

import javax.transaction.Transactional;

/**
 * Service to store and read event streams.
 */
public interface EventRepository {

    /**
     * Get a stream of envelopes
     *
     * @return the stream of envelopes. Never returns null.
     */
    Stream<JsonEnvelope> getAll();

    /**
     * Get a stream of envelopes, ordered by sequence id.
     *
     * @param streamId the id of the stream to retrieve
     * @return the stream of envelopes. Never returns null.
     */
    Stream<JsonEnvelope> getByStreamId(final UUID streamId);

    /**
     * Get a stream of envelopes from a given version, ordered by sequence id.
     *
     * @param streamId   the id of the stream to retrieve
     * @param sequenceId the sequence id to read the stream from
     * @return the stream of envelopes. Never returns null.
     */
    Stream<JsonEnvelope> getByStreamIdAndSequenceId(final UUID streamId, final Long sequenceId);

    /**
     * Stores the given envelope into the event stream.
     *
     * @param envelope the envelope containing the event and the metadata.
     * @throws StoreEventRequestFailedException If there was a failure in storing the events, this
     *                                          will wrap the underlying cause.
     */
    @Transactional
    void store(final JsonEnvelope envelope) throws StoreEventRequestFailedException;

    /**
     * Returns the latest sequence Id for the given stream id.
     *
     * @param streamId id of the stream.
     * @return latest sequence id for the stream.  Returns 0 if stream doesn't exist. Never returns
     * null.
     */
    long getCurrentSequenceIdForStream(final UUID streamId);

    /**
     * Returns stream of envelope streams. Envelopes in the nested stream are ordered by sequenceId
     *
     * @return the stream of envelope streams
     */
    Stream<Stream<JsonEnvelope>> getStreamOfAllEventStreams();

    /**
     * Returns stream of all active envelope streams. Envelopes in the nested stream are ordered by
     * sequenceId
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
    void clear(final UUID id);
}
