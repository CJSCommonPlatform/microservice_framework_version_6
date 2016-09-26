package uk.gov.justice.services.core.aggregate;

import static java.lang.String.format;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.snapshot.AggregateChangeDetectedException;
import uk.gov.justice.domain.snapshot.DefaultObjectInputStreamStrategy;
import uk.gov.justice.domain.snapshot.ObjectInputStreamStrategy;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.VersionedAggregate;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

/**
 * Service for replaying event streams on aggregates.
 */
@ApplicationScoped
public class AggregateService {

    @Inject
    Logger logger;

    @Inject
    SnapshotService snapshotService;

    @Inject
    JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private ConcurrentHashMap<String, Class<?>> eventMap = new ConcurrentHashMap<>();

    private ObjectInputStreamStrategy objectInputStreamStrategy = new DefaultObjectInputStreamStrategy();

    /**
     * Recreate an aggregate of the specified type by replaying the events from an event stream.
     *
     * @param <T>    the type parameter
     * @param stream the event stream to replay
     * @param clazz  the type of aggregate to recreate
     * @return the recreated aggregate
     */
    public <T extends Aggregate> T get(final EventStream stream, final Class<T> clazz) {
        logger.trace("Recreating aggregate for instance {} of aggregate type {}", stream.getId(), clazz);

        final Pair<Stream<JsonEnvelope>, VersionedAggregate<T>> aggregateAndEvents = latestOrChangedAggregateAndEvents(stream, clazz);

        final Stream<JsonEnvelope> filteredEvents = aggregateAndEvents.getLeft();
        final VersionedAggregate<T> versionedAggregate = aggregateAndEvents.getRight();

        final T newAggregate = applyEvents(filteredEvents, versionedAggregate.getAggregate());
        snapshotService.attemptAggregateStore(stream.getId(), stream.getCurrentVersion(), newAggregate, versionedAggregate.getVersionId());

        return newAggregate;
    }

    /**
     * Register method, invoked automatically to register all event classes into the eventMap.
     *
     * @param event identified by the framework to be registered into the event map
     */
    void register(@Observes final EventFoundEvent event) {
        logger.info("Registering event {}, {} with AggregateService", event.getEventName(), event.getClazz());
        eventMap.putIfAbsent(event.getEventName(), event.getClazz());
    }

    /**
     * Sets object input stream strategy.
     *
     * @param objectInputStreamStrategy the object input stream strategy
     */
    void setObjectInputStreamStrategy(final ObjectInputStreamStrategy objectInputStreamStrategy) {
        this.objectInputStreamStrategy = objectInputStreamStrategy;
    }

    private <T extends Aggregate> Pair<Stream<JsonEnvelope>, VersionedAggregate<T>> latestOrChangedAggregateAndEvents(final EventStream stream,
                                                                                                                      final Class<T> clazz) {
        try {
            final VersionedAggregate<T> versionedAggregate = snapshotService.getLatestVersionedAggregate(stream.getId(), clazz, objectInputStreamStrategy);
            return Pair.of(
                    stream.readFrom(versionedAggregate.getVersionId()),
                    versionedAggregate);

        } catch (AggregateChangeDetectedException e) {
            return Pair.of(
                    stream.read(),
                    snapshotService.rebuildAggregate(stream.getId(), clazz));

        }
    }

    private <T extends Aggregate> T applyEvents(final Stream<JsonEnvelope> filteredEvent, final T aggregate) {
        logger.trace("Apply Events for {}", filteredEvent);
        aggregate.apply(filteredEvent.map(this::convertEnvelopeToEvent));
        return aggregate;
    }

    private Object convertEnvelopeToEvent(final JsonEnvelope event) {
        final String name = event.metadata().name();

        if (!eventMap.containsKey(name)) {
            throw new IllegalStateException(format("No event class registered for events of type %s", name));
        }

        return jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), eventMap.get(name));
    }
}