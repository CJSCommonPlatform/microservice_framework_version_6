package uk.gov.justice.services.management.shuttering.events;

import uk.gov.justice.services.jmx.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class UnshutteringProcessStartedEvent {

    private final SystemCommand target;
    private final ZonedDateTime unshutteringStartedAt;

    public UnshutteringProcessStartedEvent(final SystemCommand target, final ZonedDateTime unshutteringStartedAt) {
        this.target = target;
        this.unshutteringStartedAt = unshutteringStartedAt;
    }

    public SystemCommand getTarget() {
        return target;
    }

    public ZonedDateTime getUnshutteringStartedAt() {
        return unshutteringStartedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof UnshutteringProcessStartedEvent)) return false;
        final UnshutteringProcessStartedEvent that = (UnshutteringProcessStartedEvent) o;
        return Objects.equals(target, that.target) &&
                Objects.equals(unshutteringStartedAt, that.unshutteringStartedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, unshutteringStartedAt);
    }

    @Override
    public String toString() {
        return "UnshutteringProcessStarted{" +
                "target=" + target +
                ", unshutteringStartedAt=" + unshutteringStartedAt +
                '}';
    }
}
