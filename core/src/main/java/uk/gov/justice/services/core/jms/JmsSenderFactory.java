package uk.gov.justice.services.core.jms;

import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to create {@link JmsSender}.
 */
@ApplicationScoped
public class JmsSenderFactory implements SenderFactory {

    @Inject
    JmsDestinations jmsDestinations;

    @Inject
    JmsEnvelopeSender jmsEnvelopeSender;

    @Inject
    SystemUserUtil systemUserUtil;

    private final Logger logger = LoggerFactory.getLogger(JmsSender.class);

    /**
     * Creates a {@link JmsSender} based on the componentDestination.
     *
     * @param componentDestination message destination component.
     * @return a new JmsSender instance.
     */
    @Override
    public Sender createSender(final String componentDestination) {
        return new JmsSender(componentDestination, jmsDestinations, jmsEnvelopeSender, logger, systemUserUtil);
    }
}
