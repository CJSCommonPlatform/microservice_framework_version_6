package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class ServiceComponentObserver {

    @Inject
    BeanInstantiater beanInstantiater;

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
                .register(beanInstantiater.instantiate(event.getHandlerBean()));
    }
}
