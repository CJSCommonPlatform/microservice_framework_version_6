package uk.gov.justice.services.eventsourcing.source.core;

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

}
