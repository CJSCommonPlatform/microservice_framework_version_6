package uk.gov.justice.services.eventsourcing.publisher.jms;

import javax.jms.Destination;

/**
 * Interface for a service that can derive what JMS destination a message should be sent to from action or event name.
 */
@FunctionalInterface
public interface MessagingDestinationResolver {

    /**
     * Resolves the JMS {@link Destination} for the provided action or event name.
     *
     * @param name action or event name.
     * @return the associated JMS destination.
     */
    Destination resolve(final String name);
}
