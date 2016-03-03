package uk.gov.justice.services.core.jms;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.jms.converter.EnvelopeConverter;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.QueueConnectionFactory;

/**
 * Factory to create {@link JmsSender}.
 */
@ApplicationScoped
public class JmsSenderFactory {

    @Inject
    JmsEndpoints jmsEndpoints;

    @Inject
    EnvelopeConverter envelopeConverter;

    @Resource(mappedName = "java:comp/DefaultJMSConnectionFactory")
    QueueConnectionFactory queueConnectionFactory;

    /**
     * Creates a {@link JmsSender} based on the componentDestination.
     *
     * @param componentDestination message destination component.
     * @return a new JmsSender instance.
     */
    public JmsSender createJmsSender(final Component componentDestination) {
        return new JmsSender(componentDestination, envelopeConverter, jmsEndpoints, queueConnectionFactory);
    }
}
