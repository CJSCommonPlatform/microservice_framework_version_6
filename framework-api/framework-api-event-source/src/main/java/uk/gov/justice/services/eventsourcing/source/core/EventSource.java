package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;

import java.util.UUID;

/**
 * Source of event streams.
 */
public interface EventSource {

    /**
     * Get a stream of events by stream id.
     *
     * @param streamId - the stream id of the stream to be retrieved
     * @return the {@link EventStream}
     */
    EventStream getStreamById(final UUID streamId);


    /**
     * Clones the stream into a new stream id.
     *
     * @param streamId - the stream id of the stream to be cloned
     * @return the StreamId of the cloned stream.
     * @throws EventStreamException if the cloning of the stream fails
     */
    UUID cloneStream(final UUID streamId) throws EventStreamException;

    /**
     * Clears all of the events from a stream id.
     *
     * @param streamId - the streamId of the stream to be cleared
     */
    void clearStream(final UUID streamId) throws EventStreamException;
}
