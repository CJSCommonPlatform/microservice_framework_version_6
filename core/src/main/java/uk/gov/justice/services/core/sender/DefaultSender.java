package uk.gov.justice.services.core.sender;


import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.jms.JmsEndpoints;
import uk.gov.justice.services.core.jms.JmsSender;
import uk.gov.justice.services.core.util.CoreUtil;
import uk.gov.justice.services.messaging.Envelope;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

/**
 * Sends an action to the next layer using JMS Sender.
 */

@Alternative
public class DefaultSender implements Sender {

    private final JmsSender jmsSender;
    private final Component destinationComponent;
    private final JmsEndpoints jmsEndpoints;

    DefaultSender(final JmsSender jmsSender, final Component destinationComponent, final JmsEndpoints jmsEndpoints) {
        this.jmsSender = jmsSender;
        this.destinationComponent = destinationComponent;
        this.jmsEndpoints = jmsEndpoints;
    }

    @Override
    public void send(final Envelope envelope) {
        final String contextName = CoreUtil.extractContextNameFromActionOrEventName(envelope.metadata().name());
        jmsSender.send(jmsEndpoints.getEndpoint(destinationComponent, contextName), envelope);
    }

}
