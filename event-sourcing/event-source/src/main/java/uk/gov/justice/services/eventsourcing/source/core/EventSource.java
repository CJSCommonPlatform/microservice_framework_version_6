package uk.gov.justice.services.eventsourcing.source.core;

import java.util.UUID;

/**
 * Source of event streams.
 */
public interface EventSource {

    /**
     * Get a stream of events by stream id.
     *
     * @param streamId the stream id
     * @return the {@link EventStreamManager}
     */
    public EventStream getStreamById(final UUID streamId);
}
