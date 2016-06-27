package uk.gov.justice.services.eventsourcing.publisher.jms;

/**
 * Interface for a service that can derive what JMS destination a message should be sent to from
 * action or event name.
 */
@FunctionalInterface
public interface EventDestinationResolver {

    /**
     * Resolves the JMS destination Name for the provided action or event name.
     *
     * @param name action or event name.
     * @return the associated JMS destination.
     */
    String destinationNameOf(final String name);
}
