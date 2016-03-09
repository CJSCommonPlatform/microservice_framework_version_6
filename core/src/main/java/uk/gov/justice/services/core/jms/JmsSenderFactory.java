package uk.gov.justice.services.core.jms;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Factory to create {@link JmsSender}.
 */
@ApplicationScoped
public class JmsSenderFactory {

    @Inject
    JmsEndpoints jmsEndpoints;

    @Inject
    JmsEnvelopeSender jmsEnvelopeSender;

    /**
     * Creates a {@link JmsSender} based on the componentDestination.
     *
     * @param componentDestination message destination component.
     * @return a new JmsSender instance.
     */
    public JmsSender createJmsSender(final Component componentDestination) {
        return new JmsSender(componentDestination, jmsEndpoints, jmsEnvelopeSender);
    }
}
