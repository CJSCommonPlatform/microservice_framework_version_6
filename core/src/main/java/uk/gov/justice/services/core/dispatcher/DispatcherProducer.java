package uk.gov.justice.services.core.dispatcher;

import static uk.gov.justice.services.core.annotation.Component.componentFromAdapter;

import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

/**
 * CDI producer for dispatchers.
 *
 * This can produce both {@link SynchronousDispatcher} and {@link AsynchronousDispatcher} beans,
 * based on the service component they are being injected into.
 *
 * Internally, the same dispatcher is used for both synchronous and asynchronous handlers; the
 * injection point in fact gets a functional interface that provides the <code>dispatch</code>
 * method. Despite this, all adapters within the same service component will be using the same
 * dispatcher instance.
 */
@ApplicationScoped
public class DispatcherProducer {

    @Inject
    BeanManager beanManager;

    private Map<Component, Dispatcher> dispatcherMap;

    public DispatcherProducer() {
        dispatcherMap = new ConcurrentHashMap<>();
    }

    /**
     * Produces the correct implementation of an asynchronous dispatcher depending on the
     * {@link Adapter} annotation at the injection point.
     *
     * @param injectionPoint class where the {@link AsynchronousDispatcher} is being injected.
     * @return the correct dispatcher instance.
     * @throws IllegalArgumentException if the injection point does not contain any adaptor
     *                                  annotations.
     */
    @Produces
    public AsynchronousDispatcher produceAsynchronousDispatcher(final InjectionPoint injectionPoint) {
        return produceDispatcher(injectionPoint)::asynchronousDispatch;
    }

    /**
     * Produces the correct implementation of a synchronous dispatcher depending on the
     * {@link Adapter} annotation at the injection point.
     *
     * @param injectionPoint class where the {@link AsynchronousDispatcher} is being injected.
     * @return the correct dispatcher instance.
     * @throws IllegalArgumentException if the injection point does not contain any adaptor
     *                                  annotations.
     */

    @Produces
    public SynchronousDispatcher produceSynchronousDispatcher(final InjectionPoint injectionPoint) {
        return produceDispatcher(injectionPoint)::synchronousDispatch;
    }

    void register(@Observes final ServiceComponentFoundEvent event) {
        getDispatcher(event.getComponent()).register(instantiateHandler(event.getHandlerBean()));
    }

    /**
     * Instantiates the handler using CDI BeanManager auto-wiring all the dependencies.
     *
     * @param bean the bean from which to get the instance
     * @return an instance of the handler
     */
    private Object instantiateHandler(final Bean<Object> bean) {
        return beanManager.getContext(bean.getScope()).get(bean, beanManager.createCreationalContext(bean));
    }

    private Dispatcher getDispatcher(final Component component) {
        return dispatcherMap.computeIfAbsent(component, c -> new Dispatcher());
    }

    private Dispatcher produceDispatcher(final InjectionPoint injectionPoint) {
        final Class<?> targetClass = injectionPoint.getMember().getDeclaringClass();

        if (targetClass.isAnnotationPresent(Adapter.class)) {
            return getDispatcher(componentFromAdapter(targetClass));
        } else {
            throw new IllegalArgumentException("InjectionPoint class must be annotated with " + ServiceComponent.class);
        }
    }
}
