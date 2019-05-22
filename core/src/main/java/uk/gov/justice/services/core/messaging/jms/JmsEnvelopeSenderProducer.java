package uk.gov.justice.services.core.messaging.jms;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.ComponentNameUtil.componentFrom;
import static uk.gov.justice.services.core.annotation.ComponentNameUtil.hasComponentAnnotation;

import uk.gov.justice.services.messaging.jms.DefaultJmsEnvelopeSender;
import uk.gov.justice.services.messaging.jms.EnvelopeSenderSelector;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;
import uk.gov.justice.services.messaging.jms.JmsSender;
import uk.gov.justice.services.messaging.jms.ShutteringJmsEnvelopeSender;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

public class JmsEnvelopeSenderProducer {

    @Inject
    private JmsSender jmsSender;

    @Inject
    private EnvelopeSenderSelector envelopeSenderSelector;

    @Produces
    public JmsEnvelopeSender createJmsEnvelopeSender(final InjectionPoint injectionPoint) {

        if (hasComponentAnnotation(injectionPoint)) {

            final String componentName = componentFrom(injectionPoint);

            if (COMMAND_API.contains(componentName)) {
                return new ShutteringJmsEnvelopeSender(envelopeSenderSelector);
            }
        }

        return new DefaultJmsEnvelopeSender(jmsSender);
    }


}
