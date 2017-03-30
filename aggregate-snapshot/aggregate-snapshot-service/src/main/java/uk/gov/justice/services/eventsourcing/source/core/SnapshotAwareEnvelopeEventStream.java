package uk.gov.justice.services.eventsourcing.source.core;

import static uk.gov.justice.services.eventsourcing.source.core.Tolerance.CONSECUTIVE;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class SnapshotAwareEnvelopeEventStream<T extends Aggregate> extends EnvelopeEventStream implements EventStream {

    private final SnapshotService snapshotService;

    private final Map<Class<T>, T> aggregatesMap = new ConcurrentHashMap<>();

    SnapshotAwareEnvelopeEventStream(final UUID id, final EventStreamManager eventStreamManager, final SnapshotService snapshotService) {
        super(id, eventStreamManager);
        this.snapshotService = snapshotService;
    }

    public void registerAggregates(final Class<T> aggregateClass, final T aggregate) {
        aggregatesMap.put(aggregateClass, aggregate);
    }

    @Override
    public long append(final Stream<JsonEnvelope> events, final Tolerance tolerance) throws EventStreamException {
        final long currentVersion = super.append(events, tolerance);
        if (tolerance == CONSECUTIVE) {
            createAggregateSnapshotsFor(currentVersion);
        }
        return currentVersion;
    }

    @Override
    public long appendAfter(final Stream<JsonEnvelope> events, final long version) throws EventStreamException {
        final long currentVersion = super.appendAfter(events, version);
        createAggregateSnapshotsFor(currentVersion);
        return currentVersion;
    }

    private void createAggregateSnapshotsFor(final long currentVersion) {
        for (final Aggregate aggregate : aggregatesMap.values()) {
            snapshotService.attemptAggregateStore(this.getId(), currentVersion, aggregate);
        }
    }

}
