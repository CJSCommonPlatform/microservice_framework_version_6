package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Source of event streams.
 */
@ApplicationScoped
public class DefaultEventSource implements EventSource {

    @Inject
    EventStreamManager eventStreamManager;

    /**
     * Get a stream of events by stream id.
     *
     * @param streamId the stream id
     * @return the {@link EventStreamManager}
     */
    public EventStream getStreamById(final UUID streamId) {
        return new EnvelopeEventStream(streamId, eventStreamManager);
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
