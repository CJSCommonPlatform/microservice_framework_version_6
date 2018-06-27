package uk.gov.justice.subscription.jms.parser;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.subscription.SubscriptionsDescriptorParser;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class SubscriptionDescriptorFileParser {

    private final SubscriptionsDescriptorParser subscriptionsDescriptorParser;
    private final PathToUrlResolver pathToUrlResolver;

    public SubscriptionDescriptorFileParser(final SubscriptionsDescriptorParser subscriptionsDescriptorParser,
                                            final PathToUrlResolver pathToUrlResolver) {
        this.subscriptionsDescriptorParser = subscriptionsDescriptorParser;
        this.pathToUrlResolver = pathToUrlResolver;
    }

    public List<SubscriptionWrapper> getSubscriptionWrappers(final Path baseDir,
                                                             final Collection<Path> paths,
                                                             final List<EventSourceDefinition> eventSourceDefinitionDefinitions) {
        final List<URL> subscriptionPaths = paths.stream()
                .filter(isNotEventSource)
                .map(path -> pathToUrlResolver.resolveToUrl(baseDir, path))
                .collect(toList());

        return subscriptionsDescriptorParser.getSubscriptionDescriptorsFrom(subscriptionPaths)
                .map(subscriptionDescriptor -> new SubscriptionWrapper(subscriptionDescriptor, eventSourceDefinitionDefinitions))
                .collect(toList());
    }

    private Predicate<Path> isNotEventSource = path -> !path.endsWith("event-sources.yaml");
}
