package uk.gov.justice.services.eventsourcing.source.api.service.core;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.Direction.FORWARD;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.eventsourcing.repository.jdbc.Direction;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

@ApplicationScoped
public class EventsService {

    @Inject
    private EventJdbcRepository repository;

    public List<EventEntry> events(final UUID streamId,
                                   final Position position,
                                   final Direction direction,
                                   final long pageSize) {

        final List<Event> eventEntries = entitiesPage(streamId, position, direction, pageSize);
        return eventEntries(eventEntries);
    }

    private List<Event> entitiesPage(
            final UUID streamId,
            final Position position,
            final Direction direction,
            final long pageSize) {
        if (position.isHead()) {
            return repository.head(streamId, pageSize).collect(toList());
        }

        if (position.isFirst()) {
            return repository.first(streamId, pageSize).collect(toList());
        }

        if (FORWARD.equals(direction)) {
            return repository.forward(streamId, position.getSequenceId(), pageSize).collect(toList());
        }

        if (BACKWARD.equals(direction)) {
            return repository.backward(streamId, position.getSequenceId(), pageSize).collect(toList());
        }

        return emptyList();
    }

    public boolean recordExists(final UUID streamId, final long sequenceId) {
        return repository.recordExists(streamId, sequenceId);
    }

    private List<EventEntry> eventEntries(final List<Event> events) {
        return events.stream()
                .map(toEventEntry())
                .collect(toList());
    }

    private Function<Event, EventEntry> toEventEntry() {
        return event -> new EventEntry(
                event.getId(),
                event.getStreamId(),
                event.getSequenceId(),
                event.getName(),
                convert(event.getPayload()),
                convertToTimestamp(event.getCreatedAt())
        );
    }

    private String convertToTimestamp(final ZonedDateTime createdAt) {
        return ZonedDateTimes.toString(createdAt);
    }

    private JsonObject convert(final String payload) {
        return new StringToJsonObjectConverter().convert(payload);
    }
}
