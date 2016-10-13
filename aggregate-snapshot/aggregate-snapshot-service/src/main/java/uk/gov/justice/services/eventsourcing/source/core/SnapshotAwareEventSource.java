package uk.gov.justice.services.eventsourcing.source.core;

import java.util.UUID;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

/**
 * Source of event streams.
 */
@ApplicationScoped
@Alternative
@Priority(10)
public class SnapshotAwareEventSource implements EventSource {

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
