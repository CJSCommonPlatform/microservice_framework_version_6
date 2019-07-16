package uk.gov.justice.services.management.shuttering.observers.shuttering;


import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ShutteringRegistry.ShutteringState.SHUTTERING_COMPLETE;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ShutteringRegistry.ShutteringState.SHUTTERING_REQUESTED;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
public class ShutteringRegistry {

    enum ShutteringState {
        SHUTTERING_REQUESTED,
        SHUTTERING_COMPLETE
    }

    @Inject
    private Event<ShutteringCompleteEvent> shutteringCompleteEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    private final List<Class<?>> allShutterables = new ArrayList<>();
    private final ConcurrentMap<Class<?>, ShutteringState> shutteringStateMap = new ConcurrentHashMap<>();

    public void registerAsShutterable(final Class<?> shutterable) {

        logger.info(format("Registering %s as shutterable", shutterable.getSimpleName()));
        allShutterables.add(shutterable);
    }

    public void shutteringStarted() {
        allShutterables.forEach(shutterableClass -> shutteringStateMap.put(shutterableClass, SHUTTERING_REQUESTED));
    }

    public void markShutteringCompleteFor(final Class<?> shutterable, final SystemCommand target) {


        logger.info("Marking shuttering complete for " + shutterable.getSimpleName());
        shutteringStateMap.put(shutterable, SHUTTERING_COMPLETE);

        if (allShutteringComplete()) {

            logger.info("All shuttering complete: " + allShutterables.stream().map(Class::getSimpleName).collect(toList()));
            shutteringStateMap.clear();
            shutteringCompleteEventFirer.fire(new ShutteringCompleteEvent(target, clock.now()));
        } 
    }

    public void clear() {
        shutteringStateMap.clear();
    }


    private boolean allShutteringComplete() {
        final Set<Class<?>> shutterables = shutteringStateMap.keySet();

        for (final Class<?> shutterable: shutterables) {
            if (shutteringStateMap.get(shutterable) != SHUTTERING_COMPLETE) {
                return false;
            }
        }

        return true;
    }
}
