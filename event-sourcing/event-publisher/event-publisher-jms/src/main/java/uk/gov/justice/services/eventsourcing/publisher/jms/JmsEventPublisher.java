package uk.gov.justice.services.eventsourcing.publisher.jms;

import uk.gov.justice.services.eventsourcing.publisher.core.EventPublisher;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * A JMS implementation of {@link EventPublisher}
 */
@ApplicationScoped
public class JmsEventPublisher implements EventPublisher {

    @Inject
    JmsEnvelopeSender jmsEnvelopeSender;

    @Inject
    Logger logger;

    @Inject
    EventDestinationResolver eventDestinationResolver;

    @Override
    public void publish(final JsonEnvelope envelope) {
        final String name = envelope.metadata().name();
        final String destination = eventDestinationResolver.destinationNameOf(name);
        logger.trace("Publishing event {} to {}", name, destination);

        jmsEnvelopeSender.send(envelope, destination);
    }

}
