package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

@ApplicationScoped
public class ServiceComponentObserver {

    @Inject
    BeanManager beanManager;

    @Inject
    DispatcherCache dispatcherCache;

    /**
     * Register a handler with a {@link Dispatcher} for the given {@link
     * ServiceComponentFoundEvent}.
     *
     * @param event the {@link ServiceComponentFoundEvent} to register
     */
    void register(@Observes final ServiceComponentFoundEvent event) {
        dispatcherCache.dispatcherFor(event)
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
}
