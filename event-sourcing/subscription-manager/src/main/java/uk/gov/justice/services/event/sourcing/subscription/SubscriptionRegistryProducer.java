package uk.gov.justice.services.event.sourcing.subscription;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDef;
import uk.gov.justice.subscription.yaml.parser.YamlParser;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SubscriptionRegistryProducer {

    @Inject
    SubscriptionDescriptorFileFinder subscriptionDescriptorFileFinder;

    @Inject
    YamlParser yamlParser;

    @Produces
    public SubscriptionDescriptorRegistry subscriptionDescriptorRegistry(@SuppressWarnings("unused") final InjectionPoint injectionPoint) {

        final List<Path> subscriptionFilePaths = subscriptionDescriptorFileFinder.findOnClasspath();

        final List<SubscriptionDescriptor> subscriptionDescriptors = subscriptionFilePaths.stream()
                .map(yamlPath -> yamlParser.parseYamlFrom(yamlPath, SubscriptionDescriptorDef.class).getSubscriptionDescriptor())
                .collect(toList());

        final Map<String, SubscriptionDescriptor> byServiceName = subscriptionDescriptors
                .stream()
                .collect(toMap(SubscriptionDescriptor::getService, subscriptionDescriptor -> subscriptionDescriptor));


        return new SubscriptionDescriptorRegistry(byServiceName);
    }
}
