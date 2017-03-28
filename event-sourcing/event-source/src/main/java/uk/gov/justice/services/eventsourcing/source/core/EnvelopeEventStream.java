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
    private volatile Optional<Long> lastReadVersion = Optional.empty();

    EnvelopeEventStream(final UUID id, final EventStreamManager eventStreamManager) {
        this.id = id;
        this.eventStreamManager = eventStreamManager;
    }

    @Override
    public Stream<JsonEnvelope> read() {
        markAsReadFrom(0L);
        return eventStreamManager.read(id).map(this::recordCurrentVersion);
    }

    @Override
    public Stream<JsonEnvelope> readFrom(final long version) {
        markAsReadFrom(version - 1);
        return eventStreamManager.readFrom(id, version).map(this::recordCurrentVersion);
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
                final Optional<Long> lastReadVersion = this.lastReadVersion;
                return lastReadVersion.isPresent() ? eventStreamManager.appendAfter(id, events, lastReadVersion.get()) : eventStreamManager.append(id, events);
        }
    }

    @Override
    public long appendAfter(final Stream<JsonEnvelope> events, final long version) throws EventStreamException {
        return eventStreamManager.appendAfter(id, events, version);
    }

    @Override
    public long getCurrentVersion() {
        final Optional<Long> lastReadVersion = this.lastReadVersion;
        return lastReadVersion.isPresent() ? lastReadVersion.get() : eventStreamManager.getCurrentVersion(id);
    }

    @Override
    public UUID getId() {
        return id;
    }

    private JsonEnvelope recordCurrentVersion(final JsonEnvelope event) {
        lastReadVersion = Optional.of(
                event.metadata().version()
                        .orElseThrow(() -> new IllegalStateException("Missing version in event from event store")));
        return event;
    }

    private synchronized void markAsReadFrom(final long version) {
        if (lastReadVersion.isPresent()) {
            throw new IllegalStateException("Event stream has already been read");
        }
        lastReadVersion = Optional.of(version);
    }
}
