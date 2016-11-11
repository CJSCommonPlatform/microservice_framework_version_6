package uk.gov.justice.services.core.dispatcher;

import static uk.gov.justice.services.core.annotation.ComponentNameUtil.componentFrom;
import static uk.gov.justice.services.core.annotation.ServiceComponentLocation.componentLocationFrom;

import uk.gov.justice.services.core.annotation.ServiceComponentLocation;
import uk.gov.justice.services.core.extension.ServiceComponentFoundEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;

@ApplicationScoped
public class DefaultDispatcherCache implements DispatcherCache {

    private final Map<Pair<String, ServiceComponentLocation>, Dispatcher> dispatcherMap = new ConcurrentHashMap<>();

    @Inject
    DispatcherFactory dispatcherFactory;

    public Dispatcher dispatcherFor(final InjectionPoint injectionPoint) {
        return createDispatcherIfAbsent(Pair.of(
                componentFrom(injectionPoint), componentLocationFrom(injectionPoint)));
    }

    public Dispatcher dispatcherFor(final ServiceComponentFoundEvent event) {
        return createDispatcherIfAbsent(Pair.of(
                event.getComponentName(), event.getLocation()));
    }

    public Dispatcher dispatcherFor(final String component, final ServiceComponentLocation location) {
        return createDispatcherIfAbsent(Pair.of(component, location));
    }

    private Dispatcher createDispatcherIfAbsent(final Pair<String, ServiceComponentLocation> component) {
        return dispatcherMap.computeIfAbsent(component, c -> dispatcherFactory.createNew());
    }
}
