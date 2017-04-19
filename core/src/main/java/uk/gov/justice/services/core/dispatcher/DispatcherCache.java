package uk.gov.justice.services.core.dispatcher;

import static uk.gov.justice.services.core.annotation.ComponentNameUtil.componentFrom;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.componentLocationFrom;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponentLocation;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Creates and caches {@link Dispatcher} for {@link InjectionPoint} or {@link
 * ServiceComponentFoundEvent}.
 */
@ApplicationScoped
public class DispatcherCache {

    private final Map<Pair<String, ServiceComponentLocation>, Dispatcher> dispatcherMap = new ConcurrentHashMap<>();

    private final DispatcherFactory dispatcherFactory = new DispatcherFactory();

    /**
     * Return a {@link Dispatcher} for the given {@link InjectionPoint}.
     *
     * @param injectionPoint the given {@link InjectionPoint}
     * @return the {@link Dispatcher}
     */
    public Dispatcher dispatcherFor(final InjectionPoint injectionPoint) {
        return createDispatcherIfAbsent(Pair.of(
                componentFrom(injectionPoint), componentLocationFrom(injectionPoint)));
    }

    /**
     * Return the {@link Dispatcher} for the given {@link ServiceComponentFoundEvent}.
     *
     * @param event the given {@link ServiceComponentFoundEvent}
     * @return the {@link Dispatcher}
     */
    public Dispatcher dispatcherFor(final ServiceComponentFoundEvent event) {
        return createDispatcherIfAbsent(Pair.of(
                event.getComponentName(), event.getLocation()));
    }

    /**
     * Return the {@link Dispatcher} for the given {@link Component} and {@link
     * ServiceComponentLocation}.
     *
     * @param component the component type for which the dispatcher is for
     * @param location  whether the dispatcher is local or remote
     * @return the {@link Dispatcher}
     */
    public Dispatcher dispatcherFor(final String component, final ServiceComponentLocation location) {
        return createDispatcherIfAbsent(Pair.of(component, location));
    }

    private Dispatcher createDispatcherIfAbsent(final Pair<String, ServiceComponentLocation> component) {
        System.out.println("createDispatcherIfAbsent for " + component);
        return dispatcherMap.computeIfAbsent(component, c -> dispatcherFactory.createNew());
    }
}
