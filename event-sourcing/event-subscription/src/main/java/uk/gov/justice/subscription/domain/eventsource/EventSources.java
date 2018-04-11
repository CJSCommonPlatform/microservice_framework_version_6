package uk.gov.justice.subscription.domain.eventsource;

import java.util.List;

public class EventSources {

    private final List<EventSource> eventSources;

    public EventSources(final List<EventSource> eventSources) {
        this.eventSources = eventSources;
    }

    public List<EventSource> getEventSources() {
        return eventSources;
    }
}
