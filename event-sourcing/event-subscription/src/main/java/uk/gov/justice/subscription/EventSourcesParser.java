package uk.gov.justice.subscription;

import uk.gov.justice.subscription.domain.eventsource.EventSource;
import uk.gov.justice.subscription.yaml.parser.YamlFileValidator;
import uk.gov.justice.subscription.yaml.parser.YamlParser;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Parse YAML URLs into {@link EventSource}s
 */
public class EventSourcesParser {
    private static final TypeReference<Map<String, List<EventSource>>> EVENT_SOURCES_TYPE_REF
            = new TypeReference<Map<String, List<EventSource>>>() {
    };

    private static final String EVENT_SOURCES = "event_sources";

    private final YamlParser yamlParser;
    private final YamlFileValidator yamlFileValidator;

    public EventSourcesParser(final YamlParser yamlParser, final YamlFileValidator yamlFileValidator) {
        this.yamlParser = yamlParser;
        this.yamlFileValidator = yamlFileValidator;
    }

    /**
     * Return a Stream of {@link EventSource} from a Collection of YAML URLs
     *
     * @param urls the YAML URLs to parse
     * @return Stream of {@link EventSource}
     */
    public Stream<EventSource> eventSourcesFrom(final Collection<URL> urls) {
        return urls.stream().flatMap(this::parseEventSourcesFromYaml);
    }

    private Stream<EventSource> parseEventSourcesFromYaml(final URL url) {
        yamlFileValidator.validateEventSource(url);
        final Map<String, List<EventSource>> stringListMap = yamlParser.parseYamlFrom(url, EVENT_SOURCES_TYPE_REF);
        return stringListMap.get(EVENT_SOURCES).stream();
    }
}
