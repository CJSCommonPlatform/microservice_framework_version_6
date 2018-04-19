package uk.gov.justice.subscription.registry;

import uk.gov.justice.subscription.EventSourcesParser;
import uk.gov.justice.subscription.YamlFileFinder;
import uk.gov.justice.subscription.domain.eventsource.EventSource;

import java.util.stream.Stream;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class EventSourceRegistryProducer {

    @Inject
    EventSourcesParser eventSourcesParser;

    @Inject
    YamlFileFinder yamlFileFinder;

    @Produces
    public EventSourceRegistry getEventSourceRegistry() {
        final Stream<EventSource> eventSourcesFrom = eventSourcesParser.getEventSourcesFrom(yamlFileFinder.getEventSourcesPaths());
        return new EventSourceRegistry(eventSourcesFrom);
    }

}
