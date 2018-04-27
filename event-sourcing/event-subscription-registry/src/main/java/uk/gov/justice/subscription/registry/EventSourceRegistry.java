package uk.gov.justice.subscription.registry;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import uk.gov.justice.subscription.domain.eventsource.EventSource;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class EventSourceRegistry {

    private final Map<String, EventSource> registry;

    public EventSourceRegistry(final Stream<EventSource> eventSources) {
        registry = eventSources.collect(toMap(
                EventSource::getName,
                eventSource -> eventSource,
                (eventSource, eventSource2) -> eventSource));
    }

    public Optional<EventSource> getEventSourceFor(final String eventSourceName) {
        return ofNullable(registry.get(eventSourceName));
    }
}
