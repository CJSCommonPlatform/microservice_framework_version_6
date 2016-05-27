package uk.gov.justice.services.core.dispatcher;

import static uk.gov.justice.services.core.annotation.Component.componentFrom;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.componentLocationFrom;

import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.annotation.ServiceComponentLocation;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;
import uk.gov.justice.services.core.sender.Sender;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;

/**
 * CDI producer for dispatchers.
 *
 * This can produce both {@link SynchronousDispatcher} and {@link AsynchronousDispatcher} beans, as
 * well as {@link Requester} beans, based on the service component they are being injected into.
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

    private Map<Pair<Component, ServiceComponentLocation>, Dispatcher> dispatcherMap;

    DispatcherProducer() {
        dispatcherMap = new ConcurrentHashMap<>();
    }

    /**
     * Produces the correct implementation of an asynchronous dispatcher depending on the {@link
     * Adapter} annotation at the injection point.
     *
     * @param injectionPoint class where the {@link AsynchronousDispatcher} is being injected
     * @return the correct dispatcher instance
     * @throws IllegalStateException if the injection point does not have an {@link Adapter}
     *                               annotation
     */
    @Produces
    public AsynchronousDispatcher produceAsynchronousDispatcher(final InjectionPoint injectionPoint) {
        return dispatcherFor(injectionPoint)::asynchronousDispatch;
    }

    public Sender produceSender(final InjectionPoint injectionPoint) {
        return dispatcherFor(injectionPoint)::asynchronousDispatch;
    }

    /**
     * Produces the correct implementation of a synchronous dispatcher depending on the {@link
     * Adapter} annotation at the injection point.
     *
     * @param injectionPoint class where the {@link AsynchronousDispatcher} is being injected
     * @return the correct dispatcher instance
     * @throws IllegalStateException if the injection point does not have an {@link Adapter}
     *                               annotation
     */
    @Produces
    public SynchronousDispatcher produceSynchronousDispatcher(final InjectionPoint injectionPoint) {
        return dispatcherFor(injectionPoint)::synchronousDispatch;
    }

    /**
     * Produces the correct implementation of a requester depending on the {@link ServiceComponent}
     * annotation at the injection point.
     *
     * @param injectionPoint class where the {@link Requester} is being injected
     * @return the correct requester instance
     * @throws IllegalStateException if the injection point does not have a {@link ServiceComponent}
     *                               annotation
     */
    @Produces
    public Requester produceRequester(final InjectionPoint injectionPoint) {
        return dispatcherFor(injectionPoint)::synchronousDispatch;
    }

    void register(@Observes final ServiceComponentFoundEvent event) {
        registerEventWithDispatcher(event);
    }

    private void registerEventWithDispatcher(final ServiceComponentFoundEvent event) {

        createDispatcherIfAbsent(Pair.of(event.getComponent(), event.getLocation()))
                .register(instantiateHandler(event.getHandlerBean()));
    }

    /**
     * Instantiates the handler using CDI BeanManager auto-wiring all the dependencies.
     *
     * @param bean the bean from which to get the instance
     * @return an instance of the handler
     */
    private <T> T instantiateHandler(final Bean<T> bean) {
        return beanManager.getContext(bean.getScope())
                .get(bean, beanManager.createCreationalContext(bean));
    }

    private Dispatcher createDispatcherIfAbsent(final Pair<Component, ServiceComponentLocation> component) {
        return dispatcherMap.computeIfAbsent(component, c -> new Dispatcher());
    }

    private Dispatcher dispatcherFor(final InjectionPoint injectionPoint) {
        return createDispatcherIfAbsent(
                Pair.of(componentFrom(injectionPoint), componentLocationFrom(injectionPoint)));
    }

}
