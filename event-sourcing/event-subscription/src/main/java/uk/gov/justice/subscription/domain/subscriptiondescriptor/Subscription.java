package uk.gov.justice.subscription.domain.subscriptiondescriptor;

import java.util.List;

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
}
