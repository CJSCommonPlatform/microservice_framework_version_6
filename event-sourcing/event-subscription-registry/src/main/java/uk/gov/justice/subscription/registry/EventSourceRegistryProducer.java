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
 * Producer for the {@link EventSourceRegistry} creates a single instance and returns the same
 * instance.
 */
@ApplicationScoped
public class EventSourceRegistryProducer {

    @Inject
    EventSourcesParser eventSourcesParser;

    @Inject
    YamlFileFinder yamlFileFinder;

    private EventSourceRegistry eventSourceRegistry;

    /**
     * Either creates the single instance of the {@link EventSourceRegistry} and returns it, or
     * returns the previously created instance.
     *
     * @return the instance of the {@link EventSourceRegistry}
     */
    @Produces
    public EventSourceRegistry getEventSourceRegistry() {

        if (null == eventSourceRegistry) {
            try {
                final Stream<EventSourceDefinition> eventSourcesFrom = eventSourcesParser.eventSourcesFrom(yamlFileFinder.getEventSourcesPaths());
                eventSourceRegistry = new EventSourceRegistry(eventSourcesFrom);

            } catch (final IOException e) {
                throw new RegistryException("Failed to find yaml/event-sources.yaml resources on the classpath", e);
            }
        }
        return eventSourceRegistry;
    }

}
