package uk.gov.justice.services.management.shuttering.api;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.gov.justice.services.jmx.state.domain.CommandState.COMPLETE;
import static uk.gov.justice.services.jmx.state.domain.CommandState.FAILED;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.state.domain.CommandState;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ShutteringResult {

    private final String shutteringExecutorName;
    private final String message;
    private final CommandState commandState;
    private final UUID commandId;
    private final SystemCommand systemCommand;
    private final Optional<Throwable> exception;

    private ShutteringResult(
            final String shutteringExecutorName,
            final String message,
            final CommandState commandState,
            final UUID commandId,
            final SystemCommand systemCommand,
            final Optional<Throwable> exception) {
        this.shutteringExecutorName = shutteringExecutorName;
        this.message = message;
        this.commandState = commandState;
        this.commandId = commandId;
        this.systemCommand = systemCommand;
        this.exception = exception;
    }

    public static ShutteringResult shutteringSucceeded(
            final String shutteringExecutorName,
            final UUID commandId,
            final String message,
            final SystemCommand systemCommand) {

        return new ShutteringResult(
                shutteringExecutorName,
                message,
                COMPLETE,
                commandId,
                systemCommand,
                empty()
        );
    }

    public static ShutteringResult shutteringFailed(
            final String shutteringExecutorName,
            final UUID commandId,
            final String message,
            final SystemCommand systemCommand) {

        return new ShutteringResult(
                shutteringExecutorName,
                message,
                FAILED,
                commandId,
                systemCommand,
                empty()
        );
    }

    public static ShutteringResult shutteringFailed(
            final String shutteringExecutorName,
            final UUID commandId,
            final String message,
            final SystemCommand systemCommand,
            final Throwable exception) {

        return new ShutteringResult(
                shutteringExecutorName,
                message,
                FAILED,
                commandId,
                systemCommand,
                of(exception)
        );
    }

    public String getShutteringExecutorName() {
        return shutteringExecutorName;
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
        if (!(o instanceof ShutteringResult)) return false;
        final ShutteringResult that = (ShutteringResult) o;
        return Objects.equals(shutteringExecutorName, that.shutteringExecutorName) &&
                Objects.equals(message, that.message) &&
                commandState == that.commandState &&
                Objects.equals(commandId, that.commandId) &&
                Objects.equals(systemCommand, that.systemCommand) &&
                Objects.equals(exception, that.exception);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shutteringExecutorName, message, commandState, commandId, systemCommand, exception);
    }

    @Override
    public String toString() {
        return "ShutteringResult{" +
                "shutteringExecutorName='" + shutteringExecutorName + '\'' +
                ", message='" + message + '\'' +
                ", commandState=" + commandState +
                ", commandId=" + commandId +
                ", systemCommand=" + systemCommand +
                ", exception=" + exception +
                '}';
    }
}
