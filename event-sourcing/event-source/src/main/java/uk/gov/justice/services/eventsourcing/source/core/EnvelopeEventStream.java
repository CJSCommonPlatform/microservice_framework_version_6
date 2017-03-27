package uk.gov.justice.services.eventsourcing.source.core;

import static uk.gov.justice.services.eventsourcing.source.core.Tolerance.CONSECUTIVE;

import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

public class EnvelopeEventStream implements EventStream {

    private final EventStreamManager eventStreamManager;
    private final UUID id;
    private Long currentVersion;
    private boolean isRead = false;

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
                return isRead ? eventStreamManager.appendAfter(id, events, currentVersion) : eventStreamManager.append(id, events);
        }
    }

    @Override
    public long appendAfter(final Stream<JsonEnvelope> events, final long version) throws EventStreamException {
        return eventStreamManager.appendAfter(id, events, version);
    }

    @Override
    public long getCurrentVersion() {
        return isRead ? currentVersion : eventStreamManager.getCurrentVersion(id);
    }

    @Override
    public UUID getId() {
        return id;
    }

    private JsonEnvelope recordCurrentVersion(final JsonEnvelope event) {
        currentVersion = event.metadata().version().orElseThrow(() -> new IllegalStateException("Missing version in event from event store"));
        return event;
    }

    private synchronized void markAsReadFrom(final long version) {
        if (isRead) {
            throw new IllegalStateException("Event stream has already been read");
        }
        isRead = true;
        currentVersion = version;
    }
}
