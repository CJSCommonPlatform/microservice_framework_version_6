package uk.gov.justice.services.management.shuttering.events;

import uk.gov.justice.services.jmx.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class UnshutteringRequestedEvent {

    private final ZonedDateTime unshutteringRequestedAt;
    private final SystemCommand systemCommand;

    public UnshutteringRequestedEvent(final SystemCommand systemCommand, final ZonedDateTime unshutteringRequestedAt) {
        this.systemCommand = systemCommand;
        this.unshutteringRequestedAt = unshutteringRequestedAt;
    }

    public SystemCommand getSystemCommand() {
        return systemCommand;
    }

    public ZonedDateTime getUnshutteringRequestedAt() {
        return unshutteringRequestedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final UnshutteringRequestedEvent that = (UnshutteringRequestedEvent) o;
        return Objects.equals(unshutteringRequestedAt, that.unshutteringRequestedAt) &&
                Objects.equals(systemCommand, that.systemCommand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unshutteringRequestedAt, systemCommand);
    }

    @Override
    public String toString() {
        return "UnshutteringRequestedEvent{" +
                "unshutteringRequestedAt=" + unshutteringRequestedAt +
                ", systemCommand=" + systemCommand +
                '}';
    }
}
