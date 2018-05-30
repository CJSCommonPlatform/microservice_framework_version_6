package uk.gov.justice.subscription.registry;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Vetoed;

/**
 * Registry containing {@link EventSourceDefinition}s mapped by the event source name
 */
@Vetoed
public class EventSourceDefinitionRegistry {

    private final Map<String, EventSourceDefinition> registry = new HashMap<>();

    private EventSourceDefinition defaultEventSourceDefinition;

    /**
     * Return an {@link EventSourceDefinition} mapped to an event source name or empty if not
     * mapped.
     *
     * @param eventSourceName the event source name to look up
     * @return Optional of {@link EventSourceDefinition} or empty
     */
    public EventSourceDefinition getEventSourceDefinitionFor(final String eventSourceName) {
        return registry.get(eventSourceName);
    }

    public EventSourceDefinition getDefaultEventSourceDefinition() {
        if (defaultEventSourceDefinition == null) {
            throw new RegistryException("You must define a default event source in event-sources.yaml");
        }
        return defaultEventSourceDefinition;
    }

    public void register(final EventSourceDefinition eventSourceDefinition) {
        if (eventSourceDefinition.isDefaultEventSource()) {
            validateDefaultEventSource(eventSourceDefinition);
            defaultEventSourceDefinition = eventSourceDefinition;
        }
        registry.put(eventSourceDefinition.getName(), eventSourceDefinition);
    }


    private void validateDefaultEventSource(EventSourceDefinition eventSourceDefinition) {
        if (defaultEventSourceDefinition != null) {
            throw new RegistryException("You cannot define more than one default event source in event-sources.yaml");
        }

        if (isDataSourceNotDefined(eventSourceDefinition)) {
            throw new RegistryException("You must define data_source for default event source in event-sources.yaml");
        }
    }

    private boolean isDataSourceNotDefined(EventSourceDefinition eventSourceDefinition) {
        return eventSourceDefinition.isDefaultEventSource() && !eventSourceDefinition.getLocation().getDataSource().isPresent();
    }
}
