package uk.gov.justice.services.eventsourcing.publisher.core;


import uk.gov.justice.services.messaging.Envelope;

/**
 * Interface for a service that can publish events.
 */
public interface EventPublisher {

    /**
     * Publish event that has been raised on the stream.
     *
     * @param envelope containing metadata and event.
     */
    void publish(final Envelope envelope);
}
