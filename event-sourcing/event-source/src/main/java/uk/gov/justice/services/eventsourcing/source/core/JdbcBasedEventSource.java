package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventStreamMetadata;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Source of event streams.
 */
public class JdbcBasedEventSource implements EventSource {

    private final EventStreamManager eventStreamManager;
    private final EventRepository eventRepository;

    public JdbcBasedEventSource(final EventStreamManager eventStreamManager, final EventRepository eventRepository) {
        this.eventStreamManager = eventStreamManager;
        this.eventRepository = eventRepository;
    }

    @Override
    public EventStream getStreamById(final UUID streamId) {
        return new EnvelopeEventStream(streamId, eventStreamManager);
    }

    @Override
    public Stream<EventStream> getStreams() {
        return eventRepository.getStreams().map(toEventStream());
    }

    @Override
    public Stream<EventStream> getStreamsFrom(final long position) {
        return eventRepository.getEventStreamsFromPosition(position)
                .map(toEventStream());
    }

    private Function<EventStreamMetadata, EventStream> toEventStream() {
        return eventStream -> new EnvelopeEventStream(eventStream.getStreamId(), eventStream.getPosition(), eventStreamManager);
    }


}
