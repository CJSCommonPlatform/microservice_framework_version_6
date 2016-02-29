package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class DispatcherProducer {

    @Inject
    BeanManager beanManager;

    private Map<Component, AsynchronousDispatcher> dispatcherMap;

    public DispatcherProducer() {
        dispatcherMap = new ConcurrentHashMap<>();
    }

    /**
     * Produces the correct implementation of the Dispatcher depending on the Adaptor annotation inside the injection point.
     *
     * @param injectionPoint Class where the {@link Dispatcher} is being injected.
     * @return The correct dispatcher instance.
     * @throws IllegalArgumentException if the injection point does not contain any adaptor annotations.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Produces
    public Dispatcher produce(final InjectionPoint injectionPoint) {
        final Class targetClass = injectionPoint.getMember().getDeclaringClass();

        if (targetClass.isAnnotationPresent(Adapter.class)) {
            return getDispatcher(Component.getComponentFromAdapter(targetClass));
        } else {
            throw new IllegalArgumentException("InjectionPoint class must be annotated with " + ServiceComponent.class);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    void register(@Observes final ServiceComponentFoundEvent event) {
        getDispatcher(event.getComponent()).register(instantiateHandler(event.getHandlerBean()));
    }

    /**
     * Instantiates the handler using CDI BeanManager auto-wiring all the dependencies.
     *
     * @param bean The bean from which to get the instance.
     * @return an instance of the handler.
     */
    private Object instantiateHandler(final Bean<Object> bean) {
        return beanManager.getContext(bean.getScope()).get(bean, beanManager.createCreationalContext(bean));
    }

    private AsynchronousDispatcher getDispatcher(final Component component) {
        dispatcherMap.putIfAbsent(component, new AsynchronousDispatcher());
        return dispatcherMap.get(component);
    }

}
