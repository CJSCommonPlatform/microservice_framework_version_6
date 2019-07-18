package uk.gov.justice.services.management.shuttering.handler;

import static uk.gov.justice.services.jmx.api.command.ShutterSystemCommand.SHUTTER;
import static uk.gov.justice.services.jmx.api.command.UnshutterSystemCommand.UNSHUTTER;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.ShutterSystemCommand;
import uk.gov.justice.services.jmx.api.command.UnshutterSystemCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;

import javax.enterprise.event.Event;
import javax.inject.Inject;

public class ShutteringSystemCommandHandler {

    @Inject
    private UtcClock clock;

    @Inject
    private Event<ShutteringRequestedEvent> shutteringRequestedEventFirer;

    @Inject
    private Event<UnshutteringRequestedEvent> unshutteringRequestedEventFirer;

    @HandlesSystemCommand(SHUTTER)
    public void onShutterRequested(final ShutterSystemCommand shutterSystemCommand) {
        shutteringRequestedEventFirer.fire(new ShutteringRequestedEvent(shutterSystemCommand, clock.now()));
    }

    @HandlesSystemCommand(UNSHUTTER)
    public void onUnshutterRequested(final UnshutterSystemCommand unshutterSystemCommand) {
        unshutteringRequestedEventFirer.fire(new UnshutteringRequestedEvent(unshutterSystemCommand, clock.now()));
    }
}
