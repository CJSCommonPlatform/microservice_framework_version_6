package uk.gov.justice.services.eventsourcing.source.core;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Source of event streams.
 */
@ApplicationScoped
public class SnapshotAwareEventSource {

    @Inject
    SnapshotAwareEventStreamManager eventStreamManager;

    /**
     * Get a stream of events by stream id.
     *
     * @param streamId the stream id
     * @return the {@link SnapshotAwareEventStreamManager}
     */
    public SnapshotAwareEnvelopeEventStream getStreamById(final UUID streamId) {
        return new SnapshotAwareEnvelopeEventStream(streamId, eventStreamManager);
    }

}
