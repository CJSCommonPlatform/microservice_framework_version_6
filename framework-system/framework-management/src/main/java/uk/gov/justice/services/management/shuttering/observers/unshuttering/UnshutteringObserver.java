package uk.gov.justice.services.management.shuttering.observers.unshuttering;

import static java.lang.String.format;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.events.UnshutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringProcessStartedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;

import java.time.ZonedDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
public class UnshutteringObserver {

    @Inject
    private Event<UnshutteringProcessStartedEvent> shutteringProcessStartedEventFirer;

    @Inject
    private UnshutteringRegistry shutteringRegistry;

    @Inject
    private Logger logger;

    @Inject
    private UtcClock clock;

    public void onUnshutteringRequested(@Observes final UnshutteringRequestedEvent unshutteringRequestedEvent) {

        final SystemCommand target = unshutteringRequestedEvent.getTarget();
        final ZonedDateTime unshutteringRequestedAt = unshutteringRequestedEvent.getUnshutteringRequestedAt();

        logger.info(format("Unshuttering requested for %s at: %s", target.getName(), unshutteringRequestedAt));

        shutteringRegistry.unshutteringStarted();

        shutteringProcessStartedEventFirer.fire(new UnshutteringProcessStartedEvent(target, clock.now()));
    }

    public void onUnshutteringComplete(@Observes final UnshutteringCompleteEvent unshutteringCompleteEvent) {

        final SystemCommand target = unshutteringCompleteEvent.getTarget();
        final ZonedDateTime unshutteringCompletedAt = unshutteringCompleteEvent.getUnshutteringCompletedAt();
        logger.info(format("Unshuttering completed for %s at: %s", target.getName(), unshutteringCompletedAt));
    }
}
