package uk.gov.justice.subscription;

import uk.gov.justice.subscription.domain.eventsource.EventSource;
import uk.gov.justice.subscription.domain.eventsource.EventSources;
import uk.gov.justice.subscription.yaml.parser.YamlFileValidator;
import uk.gov.justice.subscription.yaml.parser.YamlParser;

import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;

public class EventSourcesParser {

    private final YamlParser yamlParser;
    private final YamlFileValidator yamlFileValidator;

    public EventSourcesParser(final YamlParser yamlParser, final YamlFileValidator yamlFileValidator) {
        this.yamlParser = yamlParser;
        this.yamlFileValidator = yamlFileValidator;
    }

    public Stream<EventSource> getEventSourcesFrom(final Collection<Path> paths) {
        return paths.stream()
                .flatMap(path -> parseEventSourcesFromYaml(path).getEventSources().stream());
    }

    private EventSources parseEventSourcesFromYaml(final Path path) {
        yamlFileValidator.validateEventSource(path);
        return yamlParser.parseYamlFrom(path, EventSources.class);
    }
}
