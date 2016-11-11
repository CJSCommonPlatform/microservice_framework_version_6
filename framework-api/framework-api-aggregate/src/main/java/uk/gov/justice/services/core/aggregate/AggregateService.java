package uk.gov.justice.services.core.aggregate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;

/**
 * Service for replaying event streams on aggregates.
 */
public interface AggregateService {

    /**
     * Recreate an aggregate of the specified type by replaying the events from an event stream.
     *
     * @param <T>    the type parameter
     * @param stream the event stream to replay
     * @param clazz  the type of aggregate to recreate
     * @return the recreated aggregate
     */
    public <T extends Aggregate> T get(final EventStream stream, final Class<T> clazz);
}