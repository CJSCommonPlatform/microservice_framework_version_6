package uk.gov.justice.services.management.shuttering.observers.shuttering;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.state.domain.SystemCommandStatus.CommandState.COMPLETE;
import static uk.gov.justice.services.jmx.state.domain.SystemCommandStatus.CommandState.IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringProcessStartedEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

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
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Inject
    private Logger logger;

    @Inject
    private UtcClock clock;

    public void onShutteringRequested(@Observes final ShutteringRequestedEvent shutteringRequestedEvent) {

        final UUID commandId = shutteringRequestedEvent.getCommandId();
        final SystemCommand target = shutteringRequestedEvent.getTarget();
        final ZonedDateTime shutteringRequestedAt = shutteringRequestedEvent.getShutteringRequestedAt();
        final ZonedDateTime now = clock.now();

        logger.info(format("Shuttering requested for %s at: %s", target.getName(), shutteringRequestedAt));

        shutteringRegistry.shutteringStarted();

        shutteringProcessStartedEventFirer.fire(new ShutteringProcessStartedEvent(commandId, target, now));

        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                target,
                IN_PROGRESS,
                now,
                "Shuttering of application started"

        ));
    }

    public void onShutteringComplete(@Observes final ShutteringCompleteEvent shutteringCompleteEvent) {

        final UUID commandId = shutteringCompleteEvent.getCommandId();
        final SystemCommand target = shutteringCompleteEvent.getTarget();
        final ZonedDateTime shutteringCompleteAt = shutteringCompleteEvent.getShutteringCompleteAt();
        logger.info(format("Shuttering completed for %s at: %s", target.getName(), shutteringCompleteAt));

        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                target,
                COMPLETE,
                shutteringCompleteAt,
                "Shuttering of application complete"
        ));
    }
}
