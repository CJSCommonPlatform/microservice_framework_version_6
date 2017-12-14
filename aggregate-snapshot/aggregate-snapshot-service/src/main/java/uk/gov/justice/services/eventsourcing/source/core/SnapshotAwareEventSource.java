package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;

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
@Priority(100)
public class SnapshotAwareEventSource implements EventSource {

    @Inject
    EventStreamManager eventStreamManager;

    @Inject
    SnapshotService snapshotService;

    /**
     * Get a stream of events by stream id.
     *
     * @param streamId the stream id
     * @return the {@link SnapshotAwareEnvelopeEventStream}
     */
    public EventStream getStreamById(final UUID streamId) {
        return new SnapshotAwareEnvelopeEventStream(streamId, eventStreamManager, snapshotService);
    }

    @Override
    public UUID cloneStream(final UUID streamId) throws EventStreamException {
        return eventStreamManager.cloneAsAncestor(streamId);
    }

    @Override
    public void clearStream(final UUID streamId) throws EventStreamException {
        eventStreamManager.clear(streamId);
    }

}
