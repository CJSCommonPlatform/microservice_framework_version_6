package uk.gov.justice.services.eventsource;

import uk.gov.justice.services.core.jms.JmsEndpoints;
import uk.gov.justice.services.eventsourcing.publisher.jms.JmsEventPublisher;
import uk.gov.justice.services.eventsourcing.publisher.jms.MessagingDestinationResolver;
import uk.gov.justice.services.messaging.context.ContextName;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.Destination;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

/**
 * Provides Listener endpoint to {@link JmsEventPublisher}.
 */
@ApplicationScoped
public class EventSourceDestinationResolver implements MessagingDestinationResolver {

    @Inject
    JmsEndpoints jmsEndpoints;

    @Override
    public Destination resolve(final String name) {
        return jmsEndpoints.getEndpoint(EVENT_LISTENER, ContextName.fromName(name));
    }

}
