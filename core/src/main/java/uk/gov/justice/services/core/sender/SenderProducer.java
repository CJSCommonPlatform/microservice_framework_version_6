package uk.gov.justice.services.core.sender;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.annotation.Component.componentFrom;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.annotation.exception.MissingAnnotationException;
import uk.gov.justice.services.core.dispatcher.DispatcherProducer;
import uk.gov.justice.services.core.jms.JmsSenderFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

/**
 * Produces the correct Sender based on the injection point.
 */
@ApplicationScoped
public class SenderProducer {

    @Inject
    JmsSenderFactory jmsSenderFactory;

    @Inject
    ComponentDestination componentDestination;

    @Inject
    DispatcherProducer dispatcherProducer;

    private Map<Component, Sender> senderMap;

    public SenderProducer() {
        senderMap = new ConcurrentHashMap<>();
    }

    /**
     * Produces the correct Sender based on the injection point.
     *
     * @param injectionPoint injection point where the Sender is being injected into.
     * @return An implementation of the Sender.
     */
    @Produces
    public Sender produce(final InjectionPoint injectionPoint) {
        final Class<?> targetClass = injectionPoint.getMember().getDeclaringClass();

        if (targetClass.isAnnotationPresent(ServiceComponent.class)) {
            return getSender(componentFrom(targetClass), injectionPoint);
        } else {
            throw new MissingAnnotationException("InjectionPoint class must be annotated with " + ServiceComponent.class);
        }
    }

    private Sender getSender(final Component component, final InjectionPoint injectionPoint) {
        if (component == EVENT_PROCESSOR) {
            return dispatcherProducer.produceSender(injectionPoint);
        }

        return senderMap.computeIfAbsent(component, c -> jmsSenderFactory.createJmsSender(componentDestination.getDefault(c)));
    }

}
