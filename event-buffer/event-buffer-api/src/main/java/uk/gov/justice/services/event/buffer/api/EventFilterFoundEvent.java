package uk.gov.justice.services.event.buffer.api;

import java.util.Set;

public class EventFilterFoundEvent {

    private final Set<String> events;

    public EventFilterFoundEvent(final Set<String> events) {
        this.events = events;
    }

    public Set<String> getEvents() {
        return events;
    }
}
