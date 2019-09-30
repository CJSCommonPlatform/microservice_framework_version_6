package uk.gov.justice.services.management.shuttering.events;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class ShutteringRequestedEvent {

    private final UUID commandId;
    private final SystemCommand target;
    private final ZonedDateTime shutteringRequestedAt;

    public ShutteringRequestedEvent(
            final UUID commandId,
            final SystemCommand target,
            final ZonedDateTime shutteringRequestedAt) {
        this.commandId = commandId;
        this.target = target;
        this.shutteringRequestedAt = shutteringRequestedAt;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public SystemCommand getTarget() {
        return target;
    }

    public ZonedDateTime getShutteringRequestedAt() {
        return shutteringRequestedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ShutteringRequestedEvent)) return false;
        final ShutteringRequestedEvent that = (ShutteringRequestedEvent) o;
        return Objects.equals(commandId, that.commandId) &&
                Objects.equals(target, that.target) &&
                Objects.equals(shutteringRequestedAt, that.shutteringRequestedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, target, shutteringRequestedAt);
    }

    @Override
    public String toString() {
        return "ShutteringRequestedEvent{" +
                "commandId=" + commandId +
                ", target=" + target +
                ", shutteringRequestedAt=" + shutteringRequestedAt +
                '}';
    }
}
