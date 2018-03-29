package uk.gov.justice.services.event.sourcing.subscription;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import uk.gov.justice.subscription.domain.SubscriptionDescriptor;
import uk.gov.justice.subscription.file.read.SubscriptionDescriptorParser;

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
    SubscriptionDescriptorParser subscriptionDescriptorParser;

    @Produces
    public SubscriptionDescriptorRegistry subscriptionDescriptorRegistry(@SuppressWarnings("unused") final InjectionPoint injectionPoint) {

        final List<Path> subscriptionFilePaths = subscriptionDescriptorFileFinder.findOnClasspath();

        final List<SubscriptionDescriptor> subscriptionDescriptors = subscriptionFilePaths.stream()
                .map(subscriptionDescriptorParser::read)
                .collect(toList());

        final Map<String, SubscriptionDescriptor> byServiceName = subscriptionDescriptors
                .stream()
                .collect(toMap(SubscriptionDescriptor::getService, subscriptionDescriptor -> subscriptionDescriptor));


        return new SubscriptionDescriptorRegistry(byServiceName);
    }
}
