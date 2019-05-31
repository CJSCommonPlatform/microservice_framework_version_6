package uk.gov.justice.services.management.shuttering.events;

import uk.gov.justice.services.jmx.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class ShutteringRequestedEvent {

    private final SystemCommand systemCommand;
    private final ZonedDateTime shutteringRequestedAt;

    public ShutteringRequestedEvent(final SystemCommand systemCommand, final ZonedDateTime shutteringRequestedAt) {
        this.systemCommand = systemCommand;
        this.shutteringRequestedAt = shutteringRequestedAt;
    }

    public SystemCommand getSystemCommand() {
        return systemCommand;
    }

    public ZonedDateTime getShutteringRequestedAt() {
        return shutteringRequestedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ShutteringRequestedEvent that = (ShutteringRequestedEvent) o;
        return Objects.equals(systemCommand, that.systemCommand) &&
                Objects.equals(shutteringRequestedAt, that.shutteringRequestedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(systemCommand, shutteringRequestedAt);
    }

    @Override
    public String toString() {
        return "ShutteringRequestedEvent{" +
                "systemCommand=" + systemCommand +
                ", shutteringRequestedAt=" + shutteringRequestedAt +
                '}';
    }
}
