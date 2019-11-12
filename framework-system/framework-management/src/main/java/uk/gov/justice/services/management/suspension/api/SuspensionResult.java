package uk.gov.justice.services.management.suspension.api;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.domain.CommandState;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class SuspensionResult {

    private final String suspendableName;
    private final String message;
    private final CommandState commandState;
    private final UUID commandId;
    private final SystemCommand systemCommand;
    private final Optional<Throwable> exception;

    private SuspensionResult(
            final String suspendableName,
            final String message,
            final CommandState commandState,
            final UUID commandId,
            final SystemCommand systemCommand,
            final Optional<Throwable> exception) {
        this.suspendableName = suspendableName;
        this.message = message;
        this.commandState = commandState;
        this.commandId = commandId;
        this.systemCommand = systemCommand;
        this.exception = exception;
    }

    public static SuspensionResult suspensionSucceeded(
            final String suspendableName,
            final UUID commandId,
            final String message,
            final SystemCommand systemCommand) {

        return new SuspensionResult(
                suspendableName,
                message,
                COMMAND_COMPLETE,
                commandId,
                systemCommand,
                empty()
        );
    }

    public static SuspensionResult suspensionFailed(
            final String suspendableName,
            final UUID commandId,
            final String message,
            final SystemCommand systemCommand) {

        return new SuspensionResult(
                suspendableName,
                message,
                COMMAND_FAILED,
                commandId,
                systemCommand,
                empty()
        );
    }

    public static SuspensionResult suspensionFailed(
            final String suspendableName,
            final UUID commandId,
            final String message,
            final SystemCommand systemCommand,
            final Throwable exception) {

        return new SuspensionResult(
                suspendableName,
                message,
                COMMAND_FAILED,
                commandId,
                systemCommand,
                of(exception)
        );
    }

    public String getSuspendableName() {
        return suspendableName;
    }

    public String getMessage() {
        return message;
    }

    public CommandState getCommandState() {
        return commandState;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public SystemCommand getSystemCommand() {
        return systemCommand;
    }

    public Optional<Throwable> getException() {
        return exception;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof SuspensionResult)) return false;
        final SuspensionResult that = (SuspensionResult) o;
        return Objects.equals(suspendableName, that.suspendableName) &&
                Objects.equals(message, that.message) &&
                commandState == that.commandState &&
                Objects.equals(commandId, that.commandId) &&
                Objects.equals(systemCommand, that.systemCommand) &&
                Objects.equals(exception, that.exception);
    }

    @Override
    public int hashCode() {
        return Objects.hash(suspendableName, message, commandState, commandId, systemCommand, exception);
    }

    @Override
    public String toString() {
        return "SuspensionResult{" +
                "suspendableName='" + suspendableName + '\'' +
                ", message='" + message + '\'' +
                ", commandState=" + commandState +
                ", commandId=" + commandId +
                ", systemCommand=" + systemCommand +
                ", exception=" + exception +
                '}';
    }
}
