package uk.gov.justice.services.core.sender;

import static uk.gov.justice.services.core.annotation.Component.EVENT_API;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.annotation.ComponentNameUtil.componentFrom;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.annotation.exception.MissingAnnotationException;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.jms.JmsSenderFactory;
import uk.gov.justice.services.core.jms.JmsSenderWrapper;

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
    DispatcherCache dispatcherCache;

    private Map<String, Sender> senderMap;

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
            return getSender(componentFrom(injectionPoint), injectionPoint);
        } else {
            throw new MissingAnnotationException("InjectionPoint class must be annotated with " + ServiceComponent.class);
        }
    }

    private Sender getSender(final String componentName
            , final InjectionPoint injectionPoint) {
        final Sender primarySender = produceSender(injectionPoint);
        final Sender legacySender = !componentName.equals(EVENT_PROCESSOR.name()) && !componentName.equals(EVENT_API.name())?
                senderMap.computeIfAbsent(componentName, c -> jmsSenderFactory.createJmsSender(componentDestination.getDefault(Component.valueOf(c)))) : null;
        return new JmsSenderWrapper(primarySender, legacySender);
    }

    private Sender produceSender(final InjectionPoint injectionPoint) {
        return dispatcherCache.dispatcherFor(injectionPoint)::asynchronousDispatch;
    }

}
