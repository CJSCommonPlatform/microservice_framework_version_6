package uk.gov.justice.subscription.registry;

import uk.gov.justice.subscription.EventSourcesParser;
import uk.gov.justice.subscription.YamlFileFinder;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;

import java.io.IOException;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * Producer for the {@link EventSourceDefinitionRegistry} creates a single instance and returns the
 * same instance.
 */
@ApplicationScoped
public class EventSourceDefinitionRegistryProducer {

    @Inject
    EventSourcesParser eventSourcesParser;

    @Inject
    YamlFileFinder yamlFileFinder;

    private EventSourceDefinitionRegistry eventSourceDefinitionRegistry;

    /**
     * Either creates the single instance of the {@link EventSourceDefinitionRegistry} and returns
     * it, or returns the previously created instance.
     *
     * @return the instance of the {@link EventSourceDefinitionRegistry}
     */
    @Produces
    public EventSourceDefinitionRegistry getEventSourceDefinitionRegistry() {

        if (null == eventSourceDefinitionRegistry) {
            try {
                final Stream<EventSourceDefinition> eventSourcesFrom = eventSourcesParser.eventSourcesFrom(yamlFileFinder.getEventSourcesPaths());

                eventSourceDefinitionRegistry = new EventSourceDefinitionRegistry();
                eventSourcesFrom.forEach(eventSourceDefinition -> eventSourceDefinitionRegistry.register(eventSourceDefinition));

            } catch (final IOException e) {
                throw new RegistryException("Failed to find yaml/event-sources.yaml resources on the classpath", e);
            }
        }
        return eventSourceDefinitionRegistry;
    }
}
