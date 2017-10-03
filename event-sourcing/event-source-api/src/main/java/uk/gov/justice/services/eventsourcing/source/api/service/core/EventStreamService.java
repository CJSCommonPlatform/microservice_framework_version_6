package uk.gov.justice.services.eventsourcing.source.api.service.core;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.FORWARD;

import uk.gov.justice.services.eventsourcing.repository.jdbc.Direction;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;

import java.util.List;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EventStreamService {

    @Inject
    private EventStreamJdbcRepository repository;

    public List<EventStreamEntry> eventStream(final Position position,
                                              final Direction direction,
                                              final long pageSize) {

        final List<EventStream> eventStreamsEntries = entitiesPage(position, direction, pageSize);
        return eventStreamEntries(eventStreamsEntries);
    }

    private List<EventStream> entitiesPage(
            final Position position,
            final Direction direction,
            final long pageSize) {
        if (position.isHead()) {
            return repository.head(pageSize).collect(toList());
        }

        if (position.isFirst()) {
            return repository.first(pageSize).collect(toList());
        }

        if (FORWARD.equals(direction)) {
            return repository.forward(position.getSequenceId(), pageSize).collect(toList());
        }

        if (BACKWARD.equals(direction)) {
            return repository.backward(position.getSequenceId(), pageSize).collect(toList());
        }
        return emptyList();
    }

    public boolean recordExists(final long sequenceId) {
        return repository.recordExists(sequenceId);
    }

    private List<EventStreamEntry> eventStreamEntries(final List<EventStream> events) {
        return events.stream()
                .map(toEventStreamEntry())
                .collect(toList());
    }

    private Function<EventStream, EventStreamEntry> toEventStreamEntry() {
        return eventStream -> new EventStreamEntry(
                eventStream.getStreamId().toString(),
                eventStream.getSequenceNumber()
        );
    }
}
