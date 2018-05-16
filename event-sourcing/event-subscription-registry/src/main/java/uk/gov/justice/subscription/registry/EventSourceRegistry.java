package uk.gov.justice.subscription.registry;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;

import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/**
 * Registry containing {@link EventSourceDefinition}s mapped by the event source name
 */
public class EventSourceRegistry {

    private final Map<String, EventSourceDefinition> registry;

    private final BinaryOperator<EventSourceDefinition> registerFirstEventSourceWhenDuplicate =
            (eventSource, eventSource2) -> eventSource;

    public EventSourceRegistry(final Stream<EventSourceDefinition> eventSources) {
        registry = eventSources.collect(toMap(
                EventSourceDefinition::getName,
                eventSource -> eventSource,
                registerFirstEventSourceWhenDuplicate));
    }

    /**
     * Return an {@link EventSourceDefinition} mapped to an event source name or empty if not mapped.
     *
     * @param eventSourceName the event source name to look up
     * @return Optional of {@link EventSourceDefinition} or empty
     */
    public Optional<EventSourceDefinition> getEventSourceFor(final String eventSourceName) {
        return ofNullable(registry.get(eventSourceName));
    }
}
