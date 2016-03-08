package uk.gov.justice.services.core.jms;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.context.ContextName;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import javax.enterprise.inject.Alternative;
import java.util.Objects;

@Alternative
public class JmsSender implements Sender {

    private final JmsEndpoints jmsEndpoints;
    private final Component destinationComponent;
    private JmsEnvelopeSender jmsEnvelopeSender;

    public JmsSender(final Component destinationComponent, final JmsEndpoints jmsEndpoints,
                     JmsEnvelopeSender jmsEnvelopeSender) {
        this.destinationComponent = destinationComponent;
        this.jmsEndpoints = jmsEndpoints;
        this.jmsEnvelopeSender = jmsEnvelopeSender;
    }

    @Override
    public void send(Envelope envelope) {
        final String contextName = ContextName.fromName(envelope.metadata().name());
        jmsEnvelopeSender.send(envelope, jmsEndpoints.getEndpoint(destinationComponent, contextName));
    }

    @Override
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
