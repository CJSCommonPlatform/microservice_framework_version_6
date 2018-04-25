package uk.gov.justice.subscription.jms.parser;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.subscription.domain.eventsource.EventSources;
import uk.gov.justice.subscription.yaml.parser.YamlFileValidator;
import uk.gov.justice.subscription.yaml.parser.YamlParser;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class EventSourcesFileParser {

    private static final int FIRST_ELEMENT = 0;

    private final YamlParser yamlParser;
    private final YamlFileValidator yamlFileValidator;

    public EventSourcesFileParser(final YamlParser yamlParser, final YamlFileValidator yamlFileValidator) {
        this.yamlParser = yamlParser;
        this.yamlFileValidator = yamlFileValidator;
    }

    public EventSources getEventSources(final Path baseDir, final Collection<Path> paths) {
        final List<EventSources> eventSources = paths.stream()
                .filter(this::isEventSource)
                .map(path -> parseEventSourcesFromYaml(baseDir, path))
                .collect(toList());

        final boolean moreThenOneEventSourcesPresent = eventSources.size() > 1;
        final boolean noEventSources = eventSources.isEmpty();

        if (moreThenOneEventSourcesPresent) {
            throw new SubscriptionFileParserException("More then one event-sources.yaml files found!");
        }

        if (noEventSources) {
            throw new SubscriptionFileParserException("No event-sources.yaml files found!");
        }

        return eventSources.get(FIRST_ELEMENT);
    }

    private EventSources parseEventSourcesFromYaml(final Path baseDir,final Path path) {
        yamlFileValidator.validateEventSource(baseDir.resolve(path));
        return yamlParser.parseYamlFrom(baseDir.resolve(path), EventSources.class);
    }

    private boolean isEventSource(Path path) {
        return path.endsWith("event-sources.yaml");
    }
}
