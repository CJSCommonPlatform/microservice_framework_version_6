package uk.gov.justice.services.core.sender;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.jms.JmsEndpoints;
import uk.gov.justice.services.core.jms.JmsSender;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Produces the correct Sender based on the injection point.
 */

@ApplicationScoped
public class SenderProducer {

    @Inject
    JmsSender jmsSender;

    @Inject
    JmsEndpoints jmsEndpoints;

    @Inject
    ComponentDestination componentDestination;

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
        final Class targetClass = injectionPoint.getMember().getDeclaringClass();

        if (targetClass.isAnnotationPresent(ServiceComponent.class)) {
            return getSender(Component.getComponentFromServiceComponent(targetClass));
        } else {
            throw new IllegalArgumentException("InjectionPoint class must be annotated with " + ServiceComponent.class);
        }
    }

    private Sender getSender(final Component component) {
        if (!senderMap.containsKey(component)) {
            senderMap.put(component, new DefaultSender(jmsSender, componentDestination.getDefault(component), jmsEndpoints));
        }
        return senderMap.get(component);
    }

}
