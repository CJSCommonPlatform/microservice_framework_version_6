package uk.gov.justice.services.core.dispatcher;

import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.componentLocationFrom;

import uk.gov.justice.services.common.annotation.ComponentNameExtractor;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponentLocation;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

/**
 * Creates and caches {@link Dispatcher} for {@link InjectionPoint} or {@link
 * ServiceComponentFoundEvent}.
 */
@ApplicationScoped
public class DispatcherCache {

    private final Map<DispatcherKey, Dispatcher> dispatcherMap = new ConcurrentHashMap<>();

    private DispatcherFactory dispatcherFactory;
    private ComponentNameExtractor componentNameExtractor;

    @Inject
    public DispatcherCache(final DispatcherFactory dispatcherFactory,
                           final ComponentNameExtractor componentNameExtractor) {
        this.dispatcherFactory = dispatcherFactory;
        this.componentNameExtractor = componentNameExtractor;
    }

    public DispatcherCache() {

    }

    /**
     * Return a {@link Dispatcher} for the given {@link InjectionPoint}.
     *
     * @param injectionPoint the given {@link InjectionPoint}
     * @return the {@link Dispatcher}
     */
    public Dispatcher dispatcherFor(final InjectionPoint injectionPoint) {
        return createDispatcherIfAbsent(new DispatcherKey(
                componentNameExtractor.componentFrom(injectionPoint), componentLocationFrom(injectionPoint)));
    }

    /**
     * Return the {@link Dispatcher} for the given {@link ServiceComponentFoundEvent}.
     *
     * @param event the given {@link ServiceComponentFoundEvent}
     * @return the {@link Dispatcher}
     */
    public Dispatcher dispatcherFor(final ServiceComponentFoundEvent event) {
        return createDispatcherIfAbsent(new DispatcherKey(
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
        return createDispatcherIfAbsent(new DispatcherKey(component, location));
    }

    private Dispatcher createDispatcherIfAbsent(final DispatcherKey component) {
        return dispatcherMap.computeIfAbsent(component, c -> dispatcherFactory.createNew());
    }

    private class DispatcherKey {

        private final String componentType;
        private final ServiceComponentLocation location;

        private DispatcherKey(final String componentType, final ServiceComponentLocation location) {
            this.componentType = componentType;
            this.location = location;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final DispatcherKey that = (DispatcherKey) o;
            return Objects.equals(componentType, that.componentType) &&
                    location == that.location;
        }

        @Override
        public int hashCode() {
            return Objects.hash(componentType, location);
        }
    }
}
