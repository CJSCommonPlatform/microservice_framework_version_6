package uk.gov.justice.domain;

/**
 * Underlying interface that every domain aggregate needs to implement.
 */
public interface Aggregate {

    /**
     * Apply an event to update the state of this aggregate.
     * The provided event should be returned by the call to support chaining.
     * @param event the event to apply
     * @return the applied event
     */
    Object apply(final Object event);
}
