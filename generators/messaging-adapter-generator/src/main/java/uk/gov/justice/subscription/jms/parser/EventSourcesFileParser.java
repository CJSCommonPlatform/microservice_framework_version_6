package uk.gov.justice.subscription.jms.parser;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.subscription.EventSourcesParser;
import uk.gov.justice.subscription.domain.eventsource.EventSource;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class EventSourcesFileParser {

    private final EventSourcesParser eventSourcesParser;

    public EventSourcesFileParser(final EventSourcesParser eventSourcesParser) {
        this.eventSourcesParser = eventSourcesParser;
    }

    public List<EventSource> getEventSources(final Path baseDir, final Collection<Path> paths) {
        final List<Path> eventSourcesPaths = paths.stream()
                .filter(this::isEventSource)
                .map(baseDir::resolve)
                .collect(toList());

        final boolean noEventSources = eventSourcesPaths.isEmpty();
        if (noEventSources) {
            throw new SubscriptionFileParserException("No event-sources.yaml files found!");
        }

        return eventSourcesParser.getEventSourcesFrom(eventSourcesPaths).collect(toList());
    }

    private boolean isEventSource(Path path) {
        return path.endsWith("event-sources.yaml");
    }
}
