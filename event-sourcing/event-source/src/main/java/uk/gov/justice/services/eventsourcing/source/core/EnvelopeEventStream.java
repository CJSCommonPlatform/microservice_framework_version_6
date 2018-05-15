package uk.gov.justice.services.eventsourcing.source.core;

import static uk.gov.justice.services.eventsourcing.source.core.Tolerance.CONSECUTIVE;

import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class EnvelopeEventStream implements EventStream {

    private final EventStreamManager eventStreamManager;
    private final UUID id;
    private volatile Optional<Long> lastReadPosition = Optional.empty();
    private long position;
    private String eventSourceName;

    public EnvelopeEventStream(final UUID id, final String eventSourceName, final EventStreamManager eventStreamManager) {
        this.id = id;
        this.eventSourceName = eventSourceName;
        this.eventStreamManager = eventStreamManager;
    }

    public EnvelopeEventStream(final UUID id, final long position, final EventStreamManager eventStreamManager) {
        this.id = id;
        this.eventStreamManager = eventStreamManager;
        this.position = position;
    }

    @Override
    public Stream<JsonEnvelope> read() {
        markAsReadFrom(0L);
        return eventStreamManager.read(id).map(this::recordCurrentPosition);
    }

    @Override
    public Stream<JsonEnvelope> readFrom(final long position) {
        markAsReadFrom(position - 1);
        return eventStreamManager.readFrom(id, position).map(this::recordCurrentPosition);
    }

    @Override
    public long append(final Stream<JsonEnvelope> events) throws EventStreamException {
        return append(events, CONSECUTIVE);
    }

    @Override
    public long append(final Stream<JsonEnvelope> events, final Tolerance tolerance) throws EventStreamException {
        switch (tolerance) {
            case NON_CONSECUTIVE:
                return eventStreamManager.appendNonConsecutively(id, events);
            default:
                final Optional<Long> lastReadVersion = this.lastReadPosition;
                return lastReadVersion.isPresent()
                        ? eventStreamManager.appendAfter(id, events.map(this::incrementLastReadPosition), lastReadVersion.get())
                        : eventStreamManager.append(id, events);
        }
    }

    @Override
    public long appendAfter(final Stream<JsonEnvelope> events, final long position) throws EventStreamException {
        return eventStreamManager.appendAfter(id, events, position);
    }

    @Override
    public long getPosition() {
        if (position == 0) {
            position = eventStreamManager.getStreamPosition(id);
            return position;
        }
        return position;
    }

    @SuppressWarnings("squid:S4144")
    @Override
    public long getCurrentVersion() {
        final Optional<Long> lastReadPosition = this.lastReadPosition;
        return lastReadPosition.isPresent() ? lastReadPosition.get() : eventStreamManager.getSize(id);
    }

    @Override
    public long size() {
        final Optional<Long> lastReadPosition = this.lastReadPosition;
        return lastReadPosition.isPresent() ? lastReadPosition.get() : eventStreamManager.getSize(id);
    }

    @Override
    public UUID getId() {
        return id;
    }

    private JsonEnvelope recordCurrentPosition(final JsonEnvelope event) {
        lastReadPosition = Optional.of(
                event.metadata().position()
                        .orElseThrow(() -> new IllegalStateException("Missing version in event from event store")));
        return event;
    }

    private synchronized JsonEnvelope incrementLastReadPosition(final JsonEnvelope event) {
        lastReadPosition = lastReadPosition.map(version -> version + 1);
        return event;
    }

    private synchronized void markAsReadFrom(final long position) {
        if (lastReadPosition.isPresent()) {
            throw new IllegalStateException("Event stream has already been read");
        }
        lastReadPosition = Optional.of(position);
    }

    @Override
    public String getName() {
        return eventSourceName;
    }
}
