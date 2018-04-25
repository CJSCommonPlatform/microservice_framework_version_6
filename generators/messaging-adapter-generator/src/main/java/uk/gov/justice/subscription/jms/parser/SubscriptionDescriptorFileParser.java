package uk.gov.justice.subscription.jms.parser;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.subscription.domain.eventsource.EventSources;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDef;
import uk.gov.justice.subscription.yaml.parser.YamlFileValidator;
import uk.gov.justice.subscription.yaml.parser.YamlParser;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class SubscriptionDescriptorFileParser {

    private final YamlParser yamlParser;
    private final YamlFileValidator yamlFileValidator;

    public SubscriptionDescriptorFileParser(final YamlParser yamlParser, final YamlFileValidator yamlFileValidator) {
        this.yamlParser = yamlParser;
        this.yamlFileValidator = yamlFileValidator;
    }

    public List<SubscriptionWrapper> getSubscriptionWrappers(final Path baseDir, final Collection<Path> paths, final EventSources eventSources) {
        return paths.stream()
                .filter(path -> !isEventSource(path))
                .map(path -> getSubscriptionWrapperFromYaml(baseDir, eventSources, path))
                .collect(toList());
    }

    private SubscriptionWrapper getSubscriptionWrapperFromYaml(final Path baseDir, final EventSources eventSources, final Path path) {
        yamlFileValidator.validateSubscription(baseDir.resolve(path));
        final SubscriptionDescriptorDef subscriptionDescriptorDef = yamlParser.parseYamlFrom(baseDir.resolve(path), SubscriptionDescriptorDef.class);
        return new SubscriptionWrapper(subscriptionDescriptorDef.getSubscriptionDescriptor(), eventSources.getEventSources());
    }

    private boolean isEventSource(Path path) {
        return path.endsWith("event-sources.yaml");
    }
}
