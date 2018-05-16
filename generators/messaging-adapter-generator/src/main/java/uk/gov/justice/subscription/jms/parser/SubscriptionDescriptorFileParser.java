package uk.gov.justice.subscription.jms.parser;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.subscription.SubscriptionDescriptorsParser;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class SubscriptionDescriptorFileParser {

    private final SubscriptionDescriptorsParser subscriptionDescriptorsParser;
    private final PathToUrlResolver pathToUrlResolver;

    public SubscriptionDescriptorFileParser(final SubscriptionDescriptorsParser subscriptionDescriptorsParser,
                                            final PathToUrlResolver pathToUrlResolver) {
        this.subscriptionDescriptorsParser = subscriptionDescriptorsParser;
        this.pathToUrlResolver = pathToUrlResolver;
    }

    public List<SubscriptionWrapper> getSubscriptionWrappers(final Path baseDir,
                                                             final Collection<Path> paths,
                                                             final List<EventSourceDefinition> eventSourceDefinitions) {
        final List<URL> subscriptionPaths = paths.stream()
                .filter(isNotEventSource)
                .map(path -> pathToUrlResolver.resolveToUrl(baseDir, path))
                .collect(toList());

        return subscriptionDescriptorsParser.getSubscriptionDescriptorsFrom(subscriptionPaths)
                .map(subscriptionDescriptor -> new SubscriptionWrapper(subscriptionDescriptor, eventSourceDefinitions))
                .collect(toList());
    }

    private Predicate<Path> isNotEventSource = path -> !path.endsWith("event-sources.yaml");
}
