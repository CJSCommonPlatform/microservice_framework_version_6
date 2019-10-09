package uk.gov.justice.services.jmx.state.observers;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.domain.CommandState;
import uk.gov.justice.services.jmx.api.domain.SystemCommandStatus;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.inject.Inject;

public class SystemCommandStateHandler {

    @Inject
    private SystemCommandStateBean systemCommandStateBean;

    public void handleSystemCommandStateChanged(final SystemCommandStateChangedEvent systemCommandStateChangedEvent) {

        final UUID commandId = systemCommandStateChangedEvent.getCommandId();
        final SystemCommand systemCommand = systemCommandStateChangedEvent.getSystemCommand();
        final CommandState commandState = systemCommandStateChangedEvent.getCommandState();
        final ZonedDateTime statusChangedAt = systemCommandStateChangedEvent.getStatusChangedAt();
        final String message = systemCommandStateChangedEvent.getMessage();

        systemCommandStateBean.addSystemCommandState(new SystemCommandStatus(
                commandId,
                systemCommand.getName(),
                commandState,
                statusChangedAt,
                message
        ));
    }
}
