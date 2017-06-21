package uk.gov.justice.services.core.dispatcher;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;
import uk.gov.justice.services.core.extension.ServiceComponentScanner;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
public class ServiceComponentObserver {

    private static final Logger LOGGER = getLogger(ServiceComponentObserver.class);

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
        LOGGER.info("Registering {} handlers in class {} for component {}",
                event.getLocation(),
                event.getHandlerBean().getBeanClass().getSimpleName(),
                event.getComponentName());

        dispatcherCache.dispatcherFor(event)
                .register(beanInstantiater.instantiate(event.getHandlerBean()));
    }
}
