package uk.gov.justice.services.jmx.state.events;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.state.domain.CommandState;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class SystemCommandStateChangedEvent {

    private final UUID commandId;
    private final SystemCommand systemCommand;
    private final CommandState commandState;
    private final ZonedDateTime statusChangedAt;
    private final String message;

    public SystemCommandStateChangedEvent(
            final UUID commandId,
            final SystemCommand systemCommand,
            final CommandState commandState,
            final ZonedDateTime statusChangedAt,
            final String message) {
        this.commandId = commandId;
        this.systemCommand = systemCommand;
        this.commandState = commandState;
        this.statusChangedAt = statusChangedAt;
        this.message = message;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public SystemCommand getSystemCommand() {
        return systemCommand;
    }

    public CommandState getCommandState() {
        return commandState;
    }

    public ZonedDateTime getStatusChangedAt() {
        return statusChangedAt;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof SystemCommandStateChangedEvent)) return false;
        final SystemCommandStateChangedEvent that = (SystemCommandStateChangedEvent) o;
        return Objects.equals(commandId, that.commandId) &&
                Objects.equals(systemCommand, that.systemCommand) &&
                commandState == that.commandState &&
                Objects.equals(statusChangedAt, that.statusChangedAt) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, systemCommand, commandState, statusChangedAt, message);
    }

    @Override
    public String toString() {
        return "SystemCommandStateEvent{" +
                "commandId=" + commandId +
                ", systemCommand=" + systemCommand +
                ", commandState=" + commandState +
                ", statusChangedAt=" + statusChangedAt +
                ", message='" + message + '\'' +
                '}';
    }
}
