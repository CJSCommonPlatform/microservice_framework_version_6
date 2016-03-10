package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;

import java.util.UUID;
import java.util.stream.Stream;

/**
 * Implementation of {@link EventStream}
 */
public class EnvelopeEventStream implements EventStream {

    final EventStreamManager eventStreamManager;
    private final UUID id;


    EnvelopeEventStream(final UUID id, final EventStreamManager eventStreamManager) {
        this.id = id;
        this.eventStreamManager = eventStreamManager;
    }

    @Override
    public Stream<Envelope> read() {
        return eventStreamManager.read(id);
    }

    @Override
    public Stream<Envelope> readFrom(final Long version) {
        return eventStreamManager.readFrom(id, version);
    }

    @Override
    public void append(final Stream<Envelope> events) throws EventStreamException {
        eventStreamManager.append(id, events);
    }

    @Override
    public void appendAfter(final Stream<Envelope> events, final Long version) throws EventStreamException {
        eventStreamManager.appendAfter(id, events, version);
    }

    @Override
    public Long getCurrentVersion() {
        return eventStreamManager.getCurrentVersion(id);
    }

    @Override
    public UUID getId() {
        return id;
    }
}
