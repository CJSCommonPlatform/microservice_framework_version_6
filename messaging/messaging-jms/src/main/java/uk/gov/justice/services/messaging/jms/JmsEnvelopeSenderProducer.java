package uk.gov.justice.services.messaging.jms;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

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

            final String componentName = componentNameExtractor.componentFrom(injectionPoint);

            if (COMMAND_API.contains(componentName) || EVENT_PROCESSOR.contains(componentName)) {
                return new ShutteringJmsEnvelopeSender(envelopeSenderSelector);
            }
        }

        return new DefaultJmsEnvelopeSender(jmsSender);
    }
}
