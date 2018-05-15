package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;

import java.util.UUID;
import java.util.stream.Stream;

/**
 * Source of event streams.
 */
public class SnapshotAwareEventSource implements EventSource {

    private final EventStreamManager eventStreamManager;
    private final SnapshotService snapshotService;
    private final EventRepository eventRepository;
    private  String name;

    public SnapshotAwareEventSource(final EventStreamManager eventStreamManager,
                                    final EventRepository eventRepository,
                                    final SnapshotService snapshotService,
                                    final String name) {
        this.eventStreamManager = eventStreamManager;
        this.eventRepository = eventRepository;
        this.snapshotService = snapshotService;
        this.name = name;
    }

    @Override
    public EventStream getStreamById(final UUID streamId) {
        return new SnapshotAwareEnvelopeEventStream(streamId, eventStreamManager, snapshotService, name);
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
