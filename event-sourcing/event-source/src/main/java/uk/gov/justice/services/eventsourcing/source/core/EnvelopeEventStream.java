package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

public class EnvelopeEventStream implements EventStream {

    private final EventStreamManager eventStreamManager;
    private final UUID id;


    EnvelopeEventStream(final UUID id, final EventStreamManager eventStreamManager) {
        this.id = id;
        this.eventStreamManager = eventStreamManager;
    }

    @Override
    public Stream<JsonEnvelope> read() {
        return eventStreamManager.read(id);
    }

    @Override
    public Stream<JsonEnvelope> readFrom(final long version) {
        return eventStreamManager.readFrom(id, version);
    }

    @Override
    public long append(final Stream<JsonEnvelope> events) throws EventStreamException {
        return eventStreamManager.append(id, events);
    }

    @Override
    public long append(final Stream<JsonEnvelope> stream, final Tolerance tolerance) throws EventStreamException {
        switch (tolerance) {
            case NON_CONSECUTIVE:
                return eventStreamManager.appendNonConsecutively(id, stream);
            default:
                return eventStreamManager.append(id, stream);
        }
    }

    @Override
    public long appendAfter(final Stream<JsonEnvelope> events, final long version) throws EventStreamException {
        return eventStreamManager.appendAfter(id, events, version);
    }

    @Override
    public long getCurrentVersion() {
        return eventStreamManager.getCurrentVersion(id);
    }

    @Override
    public UUID getId() {
        return id;
    }
}
