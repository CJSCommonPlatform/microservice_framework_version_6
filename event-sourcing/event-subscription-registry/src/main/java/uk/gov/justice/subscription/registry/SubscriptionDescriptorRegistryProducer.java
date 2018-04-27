package uk.gov.justice.subscription.registry;

import uk.gov.justice.subscription.SubscriptionDescriptorsParser;
import uk.gov.justice.subscription.YamlFileFinder;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import javax.enterprise.inject.Produces;
import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SubscriptionDescriptorRegistryProducer {

    @Inject
    YamlFileFinder yamlFileFinder;

    @Inject
    SubscriptionDescriptorsParser subscriptionDescriptorsParser;

    @Produces
    public SubscriptionDescriptorRegistry subscriptionDescriptorRegistry() {
        final List<Path> subscriptionDescriptorPaths = yamlFileFinder.getSubscriptionDescriptorPaths();
        final Stream<SubscriptionDescriptor> subscriptionDescriptors = subscriptionDescriptorsParser
                .getSubscriptionDescriptorsFrom(subscriptionDescriptorPaths);

        return new SubscriptionDescriptorRegistry(subscriptionDescriptors);
    }
}
