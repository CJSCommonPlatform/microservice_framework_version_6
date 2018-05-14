package uk.gov.justice.services.eventsourcing.source.api.service.core;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.FORWARD;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EventsService {

    @Inject
    private EventSource eventSource;

    public List<EventEntry> events(final UUID streamId,
                                   final Position position,
                                   final Direction direction,
                                   final long pageSize) {

        final EventStream eventStream = eventSource.getStreamById(streamId);

        if (position.isHead()) {
            final long versionHead = eventStream.size() - pageSize + 1;
            final Stream<JsonEnvelope> events = eventStream.readFrom(versionHead).limit(pageSize);
            return eventEntries(events);
        }

        if (position.isFirst()) {
            final int versionFirst = 1;
            final Stream<JsonEnvelope> events = eventStream.readFrom(versionFirst).limit(pageSize);
            return eventEntries(events);
        }

        if (FORWARD.equals(direction)) {
            final long version = position.getPosition();
            final Stream<JsonEnvelope> events = eventStream.readFrom(version).limit(pageSize);

            return eventEntries(events);
        }

        if (BACKWARD.equals(direction)) {
            final long version = position.getPosition() - pageSize + 1;
            final Stream<JsonEnvelope> events = eventStream.readFrom(version).limit(pageSize);
            return eventEntries(events);
        }
        return emptyList();
    }

    public boolean eventExists(final UUID streamId, final long version) {
        final EventStream eventStream = eventSource.getStreamById(streamId);
        final Stream<JsonEnvelope> event = eventStream.readFrom(version);

        return event.findAny().isPresent();
    }

    private List<EventEntry> eventEntries(final Stream<JsonEnvelope> events) {
        return events
                .map(toEventEntry())
                .collect(toList());
    }

    private Function<JsonEnvelope, EventEntry> toEventEntry() {
        return event -> new EventEntry(
                event.metadata().id(),
                event.metadata().streamId().orElseThrow(() -> new IllegalStateException("Missing stream id from event store")),
                event.metadata().position().orElseThrow(() -> new IllegalStateException("Missing version from event store")),
                event.metadata().name(),
                event.payloadAsJsonObject(),
                convertToTimestamp(event.metadata().createdAt().orElseThrow(() -> new IllegalStateException("Missing created date from event store")))
        );
    }

    private String convertToTimestamp(final ZonedDateTime createdAt) {
        return ZonedDateTimes.toString(createdAt);
    }
}
