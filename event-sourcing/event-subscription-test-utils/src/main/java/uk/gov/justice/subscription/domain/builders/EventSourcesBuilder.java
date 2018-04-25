package uk.gov.justice.subscription.domain.builders;

import uk.gov.justice.subscription.domain.eventsource.EventSource;
import uk.gov.justice.subscription.domain.eventsource.EventSources;

import java.util.ArrayList;
import java.util.List;

public final class EventSourcesBuilder {
    private final List<EventSource> eventSources = new ArrayList<>();

    private EventSourcesBuilder() {
    }

    public static EventSourcesBuilder eventSources() {
        return new EventSourcesBuilder();
    }

    public EventSourcesBuilder withEventSource(final EventSource eventSource) {
        this.eventSources.add(eventSource);
        return this;
    }

    public EventSourcesBuilder withEventSources(final List<EventSource> eventSources) {
        this.eventSources.addAll(eventSources);
        return this;
    }

    public EventSources build() {
        return new EventSources(eventSources);
    }
}
