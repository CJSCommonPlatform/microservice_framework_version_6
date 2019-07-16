package uk.gov.justice.services.management.shuttering.observers.shuttering;

import static java.lang.String.format;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringProcessStartedEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;

import java.time.ZonedDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
public class ShutteringObserver {

    @Inject
    private Event<ShutteringProcessStartedEvent> shutteringProcessStartedEventFirer;

    @Inject
    private ShutteringRegistry shutteringRegistry;

    @Inject
    private Logger logger;

    @Inject
    private UtcClock clock;

    public void onShutteringRequested(@Observes final ShutteringRequestedEvent shutteringRequestedEvent) {

        final SystemCommand target = shutteringRequestedEvent.getTarget();
        final ZonedDateTime shutteringRequestedAt = shutteringRequestedEvent.getShutteringRequestedAt();

        logger.info(format("Shuttering requested for %s at: %s", target.getName(), shutteringRequestedAt));

        shutteringRegistry.shutteringStarted();

        shutteringProcessStartedEventFirer.fire(new ShutteringProcessStartedEvent(target, clock.now()));
    }

    public void onShutteringComplete(@Observes final ShutteringCompleteEvent shutteringCompleteEvent) {

        final SystemCommand target = shutteringCompleteEvent.getTarget();
        final ZonedDateTime shutteringCompleteAt = shutteringCompleteEvent.getShutteringCompleteAt();
        logger.info(format("Shuttering completed for %s at: %s", target.getName(), shutteringCompleteAt));
    }
}
