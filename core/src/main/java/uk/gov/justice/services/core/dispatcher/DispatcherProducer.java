package uk.gov.justice.services.core.dispatcher;

import static java.lang.String.format;

import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.extension.BaseServiceComponentFoundEvent;
import uk.gov.justice.services.core.extension.RemoteServiceComponentFoundEvent;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

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
        return createDispatcherIfAbsent(injectionPoint, Adapter.class, Component::componentFromAdapter)::asynchronousDispatch;
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
        return createDispatcherIfAbsent(injectionPoint, Adapter.class, Component::componentFromAdapter)::synchronousDispatch;
    }

    /**
     * Produces the correct implementation of a synchronous dispatcher depending on the {@link
     * Adapter} annotation at the injection point.
     *
     * @param injectionPoint class where the {@link AsynchronousDispatcher} is being injected.
     * @return the correct dispatcher instance.
     * @throws IllegalArgumentException if the injection point does not contain any adaptor
     *                                  annotations.
     */
    @Produces
    public Requester produceRequester(final InjectionPoint injectionPoint) {
        return createDispatcherIfAbsent(
                injectionPoint,
                ServiceComponent.class,
                Component::componentFromServiceComponent)::synchronousDispatch;
    }

    void register(@Observes final ServiceComponentFoundEvent event) {
        registerEventWithDispatcher(event);
    }

    void register(@Observes final RemoteServiceComponentFoundEvent event) {
        registerEventWithDispatcher(event);
    }

    private void registerEventWithDispatcher(final BaseServiceComponentFoundEvent event) {
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

    private <A extends Annotation> Dispatcher createDispatcherIfAbsent(final InjectionPoint injectionPoint, final Class<A> clazz, final Function<Class<?>, Component> componentFunction) {
        final Class<?> targetClass = injectionPoint.getMember().getDeclaringClass();

        if (targetClass.isAnnotationPresent(clazz)) {
            return getDispatcher(componentFunction.apply(targetClass));
        } else {
            throw new IllegalArgumentException(format("Injection point class must be annotated with %s", clazz));
        }
    }
}
