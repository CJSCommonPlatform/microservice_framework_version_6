package uk.gov.justice.subscription.domain.subscriptiondescriptor;

import java.util.List;
import java.util.Objects;

public class Subscription {

    private final String name;
    private final List<Event> events;
    private final String eventSourceName;
    private final String prioritisation;

    public Subscription(final String name,
                        final List<Event> events,
                        final String eventSourceName,
                        final String prioritisation) {
        this.name = name;
        this.events = events;
        this.eventSourceName = eventSourceName;
        this.prioritisation = prioritisation;
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

    public String getPrioritisation() {
        return prioritisation;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Subscription that = (Subscription) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(events, that.events) &&
                Objects.equals(prioritisation, that.prioritisation) &&
                Objects.equals(eventSourceName, that.eventSourceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, events,prioritisation, eventSourceName);
    }
}
