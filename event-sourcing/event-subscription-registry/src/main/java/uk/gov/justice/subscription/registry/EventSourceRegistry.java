package uk.gov.justice.subscription.registry;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import uk.gov.justice.subscription.domain.eventsource.EventSource;

import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/**
 * Registry containing {@link EventSource}s mapped by the event source name
 */
public class EventSourceRegistry {

    private final Map<String, EventSource> registry;

    private final BinaryOperator<EventSource> registerFirstEventSourceWhenDuplicate =
            (eventSource, eventSource2) -> eventSource;

    public EventSourceRegistry(final Stream<EventSource> eventSources) {
        registry = eventSources.collect(toMap(
                EventSource::getName,
                eventSource -> eventSource,
                registerFirstEventSourceWhenDuplicate));
    }

    /**
     * Return an {@link EventSource} mapped to an event source name or empty if not mapped.
     *
     * @param eventSourceName the event source name to look up
     * @return Optional of {@link EventSource} or empty
     */
    public Optional<EventSource> getEventSourceFor(final String eventSourceName) {
        return ofNullable(registry.get(eventSourceName));
    }
}
