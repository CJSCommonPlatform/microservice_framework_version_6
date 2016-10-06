package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Implementation of {@link EventStream}
 */
public class SnapshotAwareEnvelopeEventStream<T extends Aggregate> implements EventStream {

    final SnapshotAwareEventStreamManager eventStreamManager;

    private final UUID id;

    private Map<Class<T>, T> aggregateMap = new ConcurrentHashMap<>();

    SnapshotAwareEnvelopeEventStream(final UUID id, final SnapshotAwareEventStreamManager eventStreamManager) {
        this.id = id;
        this.eventStreamManager = eventStreamManager;
    }

    public void registerAggregates(Class<T> aggregateClass, T aggregate) {
        aggregateMap.put(aggregateClass, aggregate);
    }

    @Override
    public Stream<JsonEnvelope> read() {
        return eventStreamManager.read(id);
    }

    @Override
    public Stream<JsonEnvelope> readFrom(final Long version) {
        return eventStreamManager.readFrom(id, version);
    }

    @Override
    public void append(final Stream<JsonEnvelope> events) throws EventStreamException {
        eventStreamManager.append(id, events, aggregateMap);

    }

    @Override
    public void appendAfter(final Stream<JsonEnvelope> events, final Long version) throws EventStreamException {
        eventStreamManager.appendAfter(id, events, version, aggregateMap);
    }

    public Map<Class<T>, T> getAggregatesMap() {
        return aggregateMap;
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
