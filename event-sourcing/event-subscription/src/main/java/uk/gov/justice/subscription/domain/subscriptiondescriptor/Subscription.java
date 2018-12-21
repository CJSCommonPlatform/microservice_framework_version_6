package uk.gov.justice.subscription.domain.subscriptiondescriptor;

import java.util.List;
import java.util.Objects;

public class Subscription {

    private final String name;
    private final List<Event> events;
    private final String eventSourceName;

    public Subscription(final String name,
                        final List<Event> events,
                        final String eventSourceName) {
        this.name = name;
        this.events = events;
        this.eventSourceName = eventSourceName;
    }

    public String getName() {
        return name;
    }

    public List<Event> getEvents() {
        return events;
    }

    public String getEventSourceName() {
        return eventSourceName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Subscription that = (Subscription) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(events, that.events) &&
                Objects.equals(eventSourceName, that.eventSourceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, events, eventSourceName);
    }
}
