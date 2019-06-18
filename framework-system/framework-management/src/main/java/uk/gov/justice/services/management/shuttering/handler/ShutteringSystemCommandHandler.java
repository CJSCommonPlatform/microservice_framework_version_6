package uk.gov.justice.services.management.shuttering.handler;

import static uk.gov.justice.services.management.shuttering.command.ShutterSystemCommand.SHUTTER_APPLICATION;
import static uk.gov.justice.services.management.shuttering.command.UnshutterSystemCommand.UNSHUTTER_APPLICATION;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.management.shuttering.command.ShutterSystemCommand;
import uk.gov.justice.services.management.shuttering.command.UnshutterSystemCommand;
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

    @HandlesSystemCommand(SHUTTER_APPLICATION)
    public void onShutterRequested(final ShutterSystemCommand shutterSystemCommand) {
        shutteringRequestedEventFirer.fire(new ShutteringRequestedEvent(shutterSystemCommand, clock.now()));
    }

    @HandlesSystemCommand(UNSHUTTER_APPLICATION)
    public void onUnshutterRequested(final UnshutterSystemCommand unshutterSystemCommand) {
        unshutteringRequestedEventFirer.fire(new UnshutteringRequestedEvent(unshutterSystemCommand, clock.now()));
    }
}
