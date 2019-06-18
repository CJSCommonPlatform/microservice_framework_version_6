package uk.gov.justice.services.management.shuttering.events;

import uk.gov.justice.services.jmx.command.SystemCommand;

import java.time.ZonedDateTime;
import java.util.Objects;

public class UnshutteringCompleteEvent {

    private final SystemCommand target;
    private final ZonedDateTime unshutteringCompletedAt;

    public UnshutteringCompleteEvent(final SystemCommand target, final ZonedDateTime unshutteringCompletedAt) {
        this.target = target;
        this.unshutteringCompletedAt = unshutteringCompletedAt;
    }

    public SystemCommand getTarget() {
        return target;
    }

    public ZonedDateTime getUnshutteringCompletedAt() {
        return unshutteringCompletedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof UnshutteringCompleteEvent)) return false;
        final UnshutteringCompleteEvent that = (UnshutteringCompleteEvent) o;
        return Objects.equals(target, that.target) &&
                Objects.equals(unshutteringCompletedAt, that.unshutteringCompletedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, unshutteringCompletedAt);
    }

    @Override
    public String toString() {
        return "UnshutteringCompleteEvent{" +
                "target=" + target +
                ", unshutteringCompletedAt=" + unshutteringCompletedAt +
                '}';
    }
}
