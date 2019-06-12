package uk.gov.justice.services.messaging.jms;

import uk.gov.justice.services.common.annotation.ComponentNameExtractor;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

public class JmsEnvelopeSenderProducer {

    @Inject
    private JmsSender jmsSender;

    @Inject
    private EnvelopeSenderSelector envelopeSenderSelector;

    @Inject
    private ComponentNameExtractor componentNameExtractor;

    @Produces
    public JmsEnvelopeSender createJmsEnvelopeSender(final InjectionPoint injectionPoint) {

        if (componentNameExtractor.hasComponentAnnotation(injectionPoint)) {
            return new ShutteringJmsEnvelopeSender(envelopeSenderSelector);
        }

        return new DefaultJmsEnvelopeSender(jmsSender);
    }
}
