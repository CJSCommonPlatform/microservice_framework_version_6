package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;

import java.util.UUID;

/**
 * Implementation of {@link EventSourceTransformation}
 */
public class DefaultEventSourceTransformation implements EventSourceTransformation {

    private final EventStreamManager eventStreamManager;

    public DefaultEventSourceTransformation(final EventStreamManager eventStreamManager) {
        this.eventStreamManager = eventStreamManager;
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
