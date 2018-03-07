package uk.gov.justice.services.eventsourcing.source.api.service.core;

import static com.google.common.collect.Lists.reverse;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
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
        if (position.isHead()) {
            final Stream<EventStream> eventStreamsFromFirst = eventSource.getStreamsFrom(1);
            final long fromPosition = eventStreamsFromFirst.count() - pageSize + 1;
            final Stream<EventStreamEntry> eventStreamEntryStream = eventSource.getStreamsFrom(fromPosition)
                    .map(this::convertToEventStreamEntry);
            final List<EventStreamEntry> eventStreamEntryList = eventStreamEntryStream.limit(pageSize).collect(toList());
            return reverse(eventStreamEntryList);
        }

        if (position.isFirst()) {
            final Stream<EventStream> eventStreams = eventSource.getStreamsFrom(1);
            return reverse(eventStreams.map(this::convertToEventStreamEntry).limit(pageSize).collect(toList()));
        }

        if (FORWARD.equals(direction)) {
            final Stream<EventStream> eventStreams = eventSource.getStreamsFrom(position.getPosition());
            return reverse(eventStreams.map(this::convertToEventStreamEntry).limit(pageSize).collect(toList()));
        }

        if (BACKWARD.equals(direction)) {
            final long sequenceNumber = position.getPosition() - pageSize + 1;
            final Stream<EventStream> eventStreams = eventSource.getStreamsFrom(sequenceNumber);
            return reverse(eventStreams.map(this::convertToEventStreamEntry).limit(pageSize).collect(toList()));
        }
        return emptyList();
    }


    private EventStreamEntry convertToEventStreamEntry(final EventStream eventStream) {
        return new EventStreamEntry(eventStream.getId().toString(), eventStream.getPosition());
    }

    public boolean eventStreamExists(final long position) {
        final Stream<EventStream> eventStreamsBySequence = eventSource.getStreamsFrom(1);

        return eventStreamsBySequence.anyMatch(t -> t.getPosition() == position);

    }
}
