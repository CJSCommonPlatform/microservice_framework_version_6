package uk.gov.justice.subscription.jms.parser;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.subscription.EventSourcesParser;
import uk.gov.justice.subscription.domain.eventsource.EventSource;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class EventSourcesFileParser {

    private final EventSourcesParser eventSourcesParser;
    private final PathToUrlResolver pathToUrlResolver;

    public EventSourcesFileParser(final EventSourcesParser eventSourcesParser,
                                  final PathToUrlResolver pathToUrlResolver) {
        this.eventSourcesParser = eventSourcesParser;
        this.pathToUrlResolver = pathToUrlResolver;
    }

    public List<EventSource> getEventSources(final Path baseDir, final Collection<Path> paths) {
        final List<URL> eventSourcesPaths = paths.stream()
                .filter(isEventSource)
                .map(path -> pathToUrlResolver.resolveToUrl(baseDir, path))
                .collect(toList());

        if (eventSourcesPaths.isEmpty()) {
            throw new FileParserException("No event-sources.yaml files found!");
        }

        return eventSourcesParser.eventSourcesFrom(eventSourcesPaths).collect(toList());
    }

    private Predicate<Path> isEventSource = path -> path.endsWith("event-sources.yaml");
}
