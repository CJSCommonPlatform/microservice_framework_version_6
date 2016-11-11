package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponentLocation;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;

import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Creates and caches {@link Dispatcher} for {@link InjectionPoint} or {@link
 * ServiceComponentFoundEvent}.
 */
public interface DispatcherCache {

    /**
     * Return a {@link Dispatcher} for the given {@link InjectionPoint}.
     *
     * @param injectionPoint the given {@link InjectionPoint}
     * @return the {@link Dispatcher}
     */
    Dispatcher dispatcherFor(final InjectionPoint injectionPoint);

    /**
     * Return the {@link Dispatcher} for the given {@link ServiceComponentFoundEvent}.
     *
     * @param event the given {@link ServiceComponentFoundEvent}
     * @return the {@link Dispatcher}
     */
    Dispatcher dispatcherFor(final ServiceComponentFoundEvent event);

    /**
     * Return the {@link Dispatcher} for the given {@link Component} and {@link
     * ServiceComponentLocation}.
     *
     * @param component the component type for which the dispatcher is for
     * @param location  whether the dispatcher is local or remote
     * @return the {@link Dispatcher}
     */
    Dispatcher dispatcherFor(final String component, final ServiceComponentLocation location);
}
