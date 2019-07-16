package uk.gov.justice.services.management.shuttering.observers.unshuttering;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.management.shuttering.observers.unshuttering.UnshutteringRegistry.UnshutteringState.UNSHUTTERING_COMPLETE;
import static uk.gov.justice.services.management.shuttering.observers.unshuttering.UnshutteringRegistry.UnshutteringState.UNSHUTTERING_REQUESTED;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.events.UnshutteringCompleteEvent;

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
public class UnshutteringRegistry {

    enum UnshutteringState {
        UNSHUTTERING_REQUESTED,
        UNSHUTTERING_COMPLETE
    }

    @Inject
    private Event<UnshutteringCompleteEvent> unshutteringCompleteEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    private final List<Class<?>> allUnshutteringExecutors = new ArrayList<>();
    private final ConcurrentMap<Class<?>, UnshutteringState> unshutteringStateMap = new ConcurrentHashMap<>();

    public void registerAsUnshutterable(final Class<?> unshutteringExecutorClass) {

        logger.info(format("Registering %s as unshuttering executor", unshutteringExecutorClass.getSimpleName()));
        allUnshutteringExecutors.add(unshutteringExecutorClass);
    }

    public void unshutteringStarted() {
        allUnshutteringExecutors.forEach(unshutterableClass -> unshutteringStateMap.put(unshutterableClass, UNSHUTTERING_REQUESTED));
    }

    public void markUnshutteringCompleteFor(final Class<?> unshutterable, final SystemCommand target) {

        logger.info("Marking unshuttering complete for " + unshutterable.getSimpleName());
        unshutteringStateMap.put(unshutterable, UNSHUTTERING_COMPLETE);

        if (allUnshutteringComplete()) {
            logger.info("All unshuttering complete: " + allUnshutteringExecutors.stream().map(Class::getSimpleName).collect(toList()));
            unshutteringStateMap.clear();
            unshutteringCompleteEventFirer.fire(new UnshutteringCompleteEvent(target, clock.now()));
        }
    }

    public void clear() {
        unshutteringStateMap.clear();
    }

    private boolean allUnshutteringComplete() {
        final Set<Class<?>> unshutterables = unshutteringStateMap.keySet();

        for (final Class<?> unshutterable: unshutterables) {
            if (unshutteringStateMap.get(unshutterable) != UNSHUTTERING_COMPLETE) {
                return false;
            }
        }

        return true;
    }
}
