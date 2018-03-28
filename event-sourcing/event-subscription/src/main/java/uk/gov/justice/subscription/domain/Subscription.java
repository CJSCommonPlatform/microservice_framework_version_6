package uk.gov.justice.subscription.domain;

import java.util.List;

public class Subscription {

    private final String name;
    private final List<Event> events;
    private final Eventsource eventsource;

    public Subscription(final String name, final List<Event> events, final Eventsource eventsource) {
        this.name = name;
        this.events = events;
        this.eventsource = eventsource;
    }

    public String getName() {
        return name;
    }

    public List<Event> getEvents() {
        return events;
    }

    public Eventsource getEventsource() {
        return eventsource;
    }
}
