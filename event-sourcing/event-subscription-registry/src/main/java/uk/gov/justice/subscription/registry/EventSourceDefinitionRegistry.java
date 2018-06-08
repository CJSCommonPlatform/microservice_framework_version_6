package uk.gov.justice.subscription.registry;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.Vetoed;

/**
 * Registry containing {@link EventSourceDefinition}s mapped by the event source name
 */
@Vetoed
public class EventSourceDefinitionRegistry {

    private final Map<String, EventSourceDefinition> registry = new ConcurrentHashMap<>();

    private String defaultEventSourceName = EMPTY;

    /**
     * Return an {@link EventSourceDefinition} mapped to an event source name or empty if not
     * mapped.
     *
     * @param eventSourceName the event source name to look up
     * @return Optional of {@link EventSourceDefinition} or empty
     */
    public Optional<EventSourceDefinition> getEventSourceDefinitionFor(final String eventSourceName) {
        return ofNullable(registry.get(eventSourceName));
    }

    public EventSourceDefinition getDefaultEventSourceDefinition() {
        return getEventSourceDefinitionFor(defaultEventSourceName)
                .orElseThrow(() -> new RegistryException("You must define a default event source"));
    }

    public void register(final EventSourceDefinition eventSourceDefinition) {
        if (eventSourceDefinition.isDefault()) {
            validateDefaultEventSourceDefinition(eventSourceDefinition);
            defaultEventSourceName = eventSourceDefinition.getName();
        }
        registry.put(eventSourceDefinition.getName(), eventSourceDefinition);
    }


    private void validateDefaultEventSourceDefinition(final EventSourceDefinition eventSourceDefinition) {
        if (!defaultEventSourceName.isEmpty()) {
            throw new RegistryException("You cannot define more than one default event source");
        }

        if (isDataSourceNotDefined(eventSourceDefinition)) {
            throw new RegistryException("You must define data_source for default event source");
        }
    }

    private boolean isDataSourceNotDefined(EventSourceDefinition eventSourceDefinition) {
        return !eventSourceDefinition.getLocation().getDataSource().isPresent();
    }
}
