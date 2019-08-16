package uk.gov.justice.services.management.shuttering.handler;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.api.command.ShutterSystemCommand.SHUTTER;
import static uk.gov.justice.services.jmx.api.command.UnshutterSystemCommand.UNSHUTTER;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.SHUTTERED;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.UNSHUTTERED;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.ShutterSystemCommand;
import uk.gov.justice.services.jmx.api.command.UnshutterSystemCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.observers.ShutteringStateRegistry;
import uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

public class ShutteringSystemCommandHandler {

    @Inject
    private UtcClock clock;

    @Inject
    private Event<ShutteringRequestedEvent> shutteringRequestedEventFirer;

    @Inject
    private Event<UnshutteringRequestedEvent> unshutteringRequestedEventFirer;

    @Inject
    private ShutteringStateRegistry shutteringStateRegistry;

    @Inject
    private Logger logger;

    @HandlesSystemCommand(SHUTTER)
    public void onShutterRequested(final ShutterSystemCommand shutterSystemCommand) {
        final ContextShutteredState shutteredState = shutteringStateRegistry.getShutteredState();
        if(shutteredState == UNSHUTTERED) {
            shutteringRequestedEventFirer.fire(new ShutteringRequestedEvent(shutterSystemCommand, clock.now()));
        } else {
            logger.info(format("Ignoring command '%s'. Context shuttered state is '%s'", shutterSystemCommand, shutteredState));
        }
    }

    @HandlesSystemCommand(UNSHUTTER)
    public void onUnshutterRequested(final UnshutterSystemCommand unshutterSystemCommand) {
        final ContextShutteredState shutteredState = shutteringStateRegistry.getShutteredState();
        if(shutteredState == SHUTTERED) {
            unshutteringRequestedEventFirer.fire(new UnshutteringRequestedEvent(unshutterSystemCommand, clock.now()));
        } else {
            logger.info(format("Ignoring command '%s'. Context shuttered state is '%s'", unshutterSystemCommand, shutteredState));
        }
    }
}
