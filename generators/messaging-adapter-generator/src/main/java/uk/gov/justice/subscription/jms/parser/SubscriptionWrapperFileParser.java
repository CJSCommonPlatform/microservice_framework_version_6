package uk.gov.justice.subscription.jms.parser;

import uk.gov.justice.maven.generator.io.files.parser.FileParser;
import uk.gov.justice.subscription.domain.eventsource.EventSources;

import java.nio.file.Path;
import java.util.Collection;

public class SubscriptionWrapperFileParser implements FileParser<SubscriptionWrapper> {

    private final EventSourcesFileParser eventSourcesFileParser;
    private final SubscriptionDescriptorFileParser subscriptionDescriptorFileParser;

    public SubscriptionWrapperFileParser(EventSourcesFileParser eventSourcesFileParser, final SubscriptionDescriptorFileParser subscriptionDescriptorFileParser) {
        this.eventSourcesFileParser = eventSourcesFileParser;
        this.subscriptionDescriptorFileParser = subscriptionDescriptorFileParser;
    }

    @Override
    public Collection<SubscriptionWrapper> parse(final Path baseDir, final Collection<Path> paths) {
        final EventSources eventSources = eventSourcesFileParser.getEventSources(baseDir,paths);
        return subscriptionDescriptorFileParser.getSubscriptionWrappers(baseDir, paths, eventSources);
    }
}
