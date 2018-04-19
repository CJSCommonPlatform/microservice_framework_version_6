package uk.gov.justice.subscription.jms.parser;

import uk.gov.justice.maven.generator.io.files.parser.FileParser;
import uk.gov.justice.subscription.domain.eventsource.EventSource;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class SubscriptionWrapperFileParser implements FileParser<SubscriptionWrapper> {

    private final EventSourcesFileParser eventSourcesFileParser;
    private final SubscriptionDescriptorFileParser subscriptionDescriptorFileParser;

    public SubscriptionWrapperFileParser(final EventSourcesFileParser eventSourcesFileParser,
                                         final SubscriptionDescriptorFileParser subscriptionDescriptorFileParser) {
        this.eventSourcesFileParser = eventSourcesFileParser;
        this.subscriptionDescriptorFileParser = subscriptionDescriptorFileParser;
    }

    @Override
    public Collection<SubscriptionWrapper> parse(final Path baseDir, final Collection<Path> paths) {
        final List<EventSource> eventSourceDefinitions = eventSourcesFileParser.getEventSources(baseDir, paths);
        return subscriptionDescriptorFileParser.getSubscriptionWrappers(baseDir, paths, eventSourceDefinitions);
    }
}
