package uk.gov.justice.subscription.domain.builders;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;

import java.util.ArrayList;
import java.util.List;

public final class EventSourcesBuilder {
    private final List<EventSourceDefinition> eventSources = new ArrayList<>();

    private EventSourcesBuilder() {
    }

    public static EventSourcesBuilder eventSources() {
        return new EventSourcesBuilder();
    }

    public EventSourcesBuilder withEventSource(final EventSourceDefinition eventSource) {
        this.eventSources.add(eventSource);
        return this;
    }

    public EventSourcesBuilder withEventSources(final List<EventSourceDefinition> eventSources) {
        this.eventSources.addAll(eventSources);
        return this;
    }

    public EventSourcesDefinitionCollection build() {
        return new EventSourcesDefinitionCollection(eventSources);
    }
}
