package uk.gov.justice.services.eventsourcing.publisher.jms;

import uk.gov.justice.services.eventsourcing.publisher.core.EventPublisher;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * A JMS implementation of {@link EventPublisher}
 */
@ApplicationScoped
public class JmsEventPublisher implements EventPublisher {

    @Inject
    JmsEnvelopeSender jmsEnvelopeSender;

    @Inject
    MessagingDestinationResolver messagingDestinationResolver;

    @Override
    public void publish(final JsonEnvelope jsonEnvelope) {
        jmsEnvelopeSender.send(jsonEnvelope, messagingDestinationResolver.resolve(jsonEnvelope.metadata().name()));
    }

}
