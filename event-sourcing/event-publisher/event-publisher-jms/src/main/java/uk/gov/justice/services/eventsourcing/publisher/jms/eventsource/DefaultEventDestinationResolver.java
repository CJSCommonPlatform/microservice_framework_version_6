package uk.gov.justice.services.eventsourcing.publisher.jms.eventsource;

import uk.gov.justice.services.eventsourcing.publisher.jms.EventDestinationResolver;
import uk.gov.justice.services.eventsourcing.publisher.jms.JmsEventPublisher;
import uk.gov.justice.services.messaging.context.ContextName;

import javax.enterprise.context.ApplicationScoped;

/**
 * Provides Listener endpoint name to {@link JmsEventPublisher}.
 */
@ApplicationScoped
public class DefaultEventDestinationResolver implements EventDestinationResolver {

    @Override
    public String destinationNameOf(final String name) {
        return String.format("%s.event", ContextName.fromName(name));
    }

}
