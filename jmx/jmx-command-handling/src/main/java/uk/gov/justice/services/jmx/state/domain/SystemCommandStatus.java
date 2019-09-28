package uk.gov.justice.services.jmx.state.domain;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class SystemCommandStatus {

    private final UUID commandId;
    private final String systemCommand;
    private final CommandState commandState;
    private final ZonedDateTime statusChangedAt;
    private final String message;

    public enum CommandState {
        IN_PROGRESS,
        COMPLETE,
        FAILED
    }

    public SystemCommandStatus(
            final UUID commandId,
            final String systemCommand,
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

    public String getSystemCommand() {
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
        if (!(o instanceof SystemCommandStatus)) return false;
        final SystemCommandStatus that = (SystemCommandStatus) o;
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
        return "CommandStatus{" +
                "commandId=" + commandId +
                ", systemCommand=" + systemCommand +
                ", commandState=" + commandState +
                ", statusChangedAt=" + statusChangedAt +
                ", message=" + message +
                '}';
    }
}
