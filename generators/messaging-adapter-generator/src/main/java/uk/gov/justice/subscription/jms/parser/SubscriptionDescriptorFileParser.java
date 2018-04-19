package uk.gov.justice.subscription.jms.parser;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.subscription.SubscriptionDescriptorsParser;
import uk.gov.justice.subscription.domain.eventsource.EventSource;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class SubscriptionDescriptorFileParser {

    private final SubscriptionDescriptorsParser subscriptionDescriptorsParser;

    public SubscriptionDescriptorFileParser(final SubscriptionDescriptorsParser subscriptionDescriptorsParser) {
        this.subscriptionDescriptorsParser = subscriptionDescriptorsParser;
    }

    public List<SubscriptionWrapper> getSubscriptionWrappers(final Path baseDir, final Collection<Path> paths, final List<EventSource> eventSourceDefinitions) {
        final List<Path> subscriptionPaths = paths.stream()
                .filter(path -> !isEventSource(path))
                .map(baseDir::resolve)
                .collect(toList());

        return subscriptionDescriptorsParser.getSubscriptionDescriptorsFrom(subscriptionPaths)
                .map(subscriptionDescriptor -> new SubscriptionWrapper(subscriptionDescriptor, eventSourceDefinitions))
                .collect(toList());
    }

    private boolean isEventSource(Path path) {
        return path.endsWith("event-sources.yaml");
    }
}
