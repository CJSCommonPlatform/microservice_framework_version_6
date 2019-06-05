package uk.gov.justice.services.management.shuttering.events;

import java.time.ZonedDateTime;
import java.util.Objects;

public class ShutteringCompleteEvent {

    private final ZonedDateTime shutteringCompleteAt;

    public ShutteringCompleteEvent(final ZonedDateTime shutteringCompleteAt) {
        this.shutteringCompleteAt = shutteringCompleteAt;
    }

    public ZonedDateTime getShutteringCompleteAt() {
        return shutteringCompleteAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ShutteringCompleteEvent)) return false;
        final ShutteringCompleteEvent that = (ShutteringCompleteEvent) o;
        return Objects.equals(shutteringCompleteAt, that.shutteringCompleteAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shutteringCompleteAt);
    }

    @Override
    public String toString() {
        return "ShutteringCompleteEvent{" +
                "shutteringCompleteAt=" + shutteringCompleteAt +
                '}';
    }
}
