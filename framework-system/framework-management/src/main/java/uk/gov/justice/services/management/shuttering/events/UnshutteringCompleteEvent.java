package uk.gov.justice.services.management.shuttering.events;

import java.time.ZonedDateTime;
import java.util.Objects;

public class UnshutteringCompleteEvent {

    private final ZonedDateTime unshutteringCompletedAt;

    public UnshutteringCompleteEvent(final ZonedDateTime unshutteringCompletedAt) {
        this.unshutteringCompletedAt = unshutteringCompletedAt;
    }

    public ZonedDateTime getUnshutteringCompletedAt() {
        return unshutteringCompletedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof UnshutteringCompleteEvent)) return false;
        final UnshutteringCompleteEvent that = (UnshutteringCompleteEvent) o;
        return Objects.equals(unshutteringCompletedAt, that.unshutteringCompletedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unshutteringCompletedAt);
    }

    @Override
    public String toString() {
        return "UnshutteringCompleteEvent{" +
                "unshutteringCompletedAt=" + unshutteringCompletedAt +
                '}';
    }
}
