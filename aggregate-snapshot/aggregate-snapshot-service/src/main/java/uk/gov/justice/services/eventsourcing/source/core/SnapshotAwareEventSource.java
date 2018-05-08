package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;

import java.util.UUID;
import java.util.stream.Stream;

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

    @Inject
    EventRepository eventRepository;

    @Override
    public EventStream getStreamById(final UUID streamId) {
        return new SnapshotAwareEnvelopeEventStream(streamId, eventStreamManager, snapshotService);
    }

    @Override
    public Stream<EventStream> getStreams() {
        return eventRepository.getStreams()
                .map(e -> new EnvelopeEventStream(e.getStreamId(), e.getPosition(),
                        eventStreamManager));
    }

    @Override
    public Stream<EventStream> getStreamsFrom(long position) {
        return eventRepository.getEventStreamsFromPosition(position)
                .map(e -> new EnvelopeEventStream(e.getStreamId(), e.getPosition(),
                        eventStreamManager));

    }

}
