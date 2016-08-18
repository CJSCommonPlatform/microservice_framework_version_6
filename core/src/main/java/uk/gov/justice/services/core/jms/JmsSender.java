package uk.gov.justice.services.core.jms;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.context.ContextName;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import java.util.Objects;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.jms.Destination;

import org.slf4j.Logger;

@Alternative
public class JmsSender implements Sender {

    final JmsDestinations jmsDestinations;
    final Component destinationComponent;
    final JmsEnvelopeSender jmsEnvelopeSender;
    final Logger logger;

    public JmsSender(
            final Component destinationComponent,
            final JmsDestinations jmsDestinations,
            final JmsEnvelopeSender jmsEnvelopeSender,
            final Logger logger) {
        this.destinationComponent = destinationComponent;
        this.jmsDestinations = jmsDestinations;
        this.jmsEnvelopeSender = jmsEnvelopeSender;
        this.logger = logger;
    }

    @Override
    public void send(final JsonEnvelope envelope) {
        final String contextName = ContextName.fromName(envelope.metadata().name());
        final Destination destination = jmsDestinations.getDestination(destinationComponent, contextName);
        logger.trace("Sending envelope for action {} to destination: {}", envelope.metadata().name(), destination);
        jmsEnvelopeSender.send(envelope, destination);

    }

    @Override
    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JmsSender jmsSender = (JmsSender) o;
        return destinationComponent == jmsSender.destinationComponent;
    }

    @Override
    public int hashCode() {
        return Objects.hash(destinationComponent);
    }

}
