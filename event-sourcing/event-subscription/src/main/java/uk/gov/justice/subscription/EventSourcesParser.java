package uk.gov.justice.subscription;

import uk.gov.justice.subscription.domain.eventsource.EventSource;
import uk.gov.justice.subscription.domain.eventsource.EventSources;
import uk.gov.justice.subscription.yaml.parser.YamlFileValidator;
import uk.gov.justice.subscription.yaml.parser.YamlParser;

import java.net.URL;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Parse YAML URLs into {@link EventSource}s
 */
public class EventSourcesParser {

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
    public Stream<EventSource> getEventSourcesFrom(final Collection<URL> urls) {
        return urls.stream()
                .flatMap(path -> parseEventSourcesFromYaml(path).getEventSources().stream());
    }

    private EventSources parseEventSourcesFromYaml(final URL url) {
        yamlFileValidator.validateEventSource(url);
        return yamlParser.parseYamlFrom(url, EventSources.class);
    }
}
