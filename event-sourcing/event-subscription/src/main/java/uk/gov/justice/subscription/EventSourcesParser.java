package uk.gov.justice.subscription;

import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.yaml.parser.YamlFileValidator;
import uk.gov.justice.subscription.yaml.parser.YamlParser;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Parse YAML URLs into {@link EventSourceDefinition}s
 */
public class EventSourcesParser {

    private final YamlParser yamlParser;
    private final YamlFileValidator yamlFileValidator;

    public EventSourcesParser(final YamlParser yamlParser, final YamlFileValidator yamlFileValidator) {
        this.yamlParser = yamlParser;
        this.yamlFileValidator = yamlFileValidator;
    }

    /**
     * Return a Stream of {@link EventSourceDefinition} from a Collection of YAML URLs
     *
     * @param urls the YAML URLs to parse
     * @return Stream of {@link EventSourceDefinition}
     */
    public List<List<EventSourceDefinition>> getEventSourcesFrom(final Collection<URL> urls) {
        final List<List<EventSourceDefinition>> eventSourceDefinitions = new ArrayList<>();
        for (URL url : urls) {
            eventSourceDefinitions.add(parseEventSourcesFromYaml(url));

        }
        return eventSourceDefinitions;
    }

    private List<EventSourceDefinition> parseEventSourcesFromYaml(final URL url) {
        yamlFileValidator.validateEventSource(url);
        return yamlParser.parseYamlFrom(url, List.class);
    }
}
