package uk.gov.justice.services.eventsourcing.source.api.service.core;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.empty;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.FORWARD;

import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;

import java.util.List;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EventStreamService {

    @Inject
    private EventSource eventSource;

    public List<EventStreamEntry> eventStreams(final Position position,
                                               final Direction direction,
                                               final long pageSize) {

        return convertToEntriesAndLimitSize(
                getEventStreams(position, direction, pageSize),
                pageSize);

    }

    private Stream<EventStream> getEventStreams(final Position position,
                                                final Direction direction,
                                                final long pageSize) {

        if (position.isHead()) {
            final Stream<EventStream> eventStreamsFromFirst = eventSource.getStreamsFrom(1);
            final long fromPosition = eventStreamsFromFirst.count() - pageSize + 1L;
            return eventSource.getStreamsFrom(fromPosition);
        }

        if (position.isFirst()) {
            return eventSource.getStreamsFrom(1);
        }

        if (FORWARD.equals(direction)) {
            return eventSource.getStreamsFrom(position.getPosition());
        }

        if (BACKWARD.equals(direction)) {
            final long sequenceNumber = position.getPosition() - pageSize + 1L;
            return eventSource.getStreamsFrom(sequenceNumber);
        }

        return empty();
    }

    private List<EventStreamEntry> convertToEntriesAndLimitSize(
            final Stream<EventStream> eventStreams,
            final long pageSize) {

        return eventStreams
                .map(this::convertToEventStreamEntry)
                .limit(pageSize)
                .collect(toList());
    }

    private EventStreamEntry convertToEventStreamEntry(final EventStream eventStream) {

        return new EventStreamEntry(
                eventStream.getId().toString(),
                eventStream.getPosition());
    }

    public boolean eventStreamExists(final long position) {

        return eventSource.getStreamsFrom(1)
                .anyMatch(eventStream -> eventStream.getPosition() == position);
    }
}
