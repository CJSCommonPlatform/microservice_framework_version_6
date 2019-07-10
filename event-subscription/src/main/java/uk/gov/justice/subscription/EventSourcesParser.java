package uk.gov.justice.subscription;

import uk.gov.justice.services.yaml.YamlFileValidator;
import uk.gov.justice.services.yaml.YamlParser;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Parse YAML URLs into {@link EventSourceDefinition}s
 */
public class EventSourcesParser {

    private static final String EVENT_SOURCES_SCHEMA_PATH = "/json/schema/event-source-schema.json";
    private static final String EVENT_SOURCES = "event_sources";
    private static final TypeReference<Map<String, List<EventSourceDefinition>>> EVENT_SOURCES_TYPE_REF
            = new TypeReference<Map<String, List<EventSourceDefinition>>>() {
    };

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
    public Stream<EventSourceDefinition> eventSourcesFrom(final List<URL> urls) {
        return urls.stream().flatMap(this::parseEventSourcesFromYaml);
    }

    private Stream<EventSourceDefinition> parseEventSourcesFromYaml(final URL url) {
        yamlFileValidator.validate(EVENT_SOURCES_SCHEMA_PATH, url);
        final Map<String, List<EventSourceDefinition>> stringListMap = yamlParser.parseYamlFrom(url, EVENT_SOURCES_TYPE_REF);
        return stringListMap.get(EVENT_SOURCES).stream();
    }
}
