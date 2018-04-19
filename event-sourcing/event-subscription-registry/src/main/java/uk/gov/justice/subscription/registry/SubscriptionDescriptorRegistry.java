package uk.gov.justice.subscription.registry;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class SubscriptionDescriptorRegistry {

    private final Map<String, SubscriptionDescriptor> registry;

    public SubscriptionDescriptorRegistry(final Stream<SubscriptionDescriptor> subscriptionDescriptors) {
        this.registry = subscriptionDescriptors
                .collect(toMap(
                        SubscriptionDescriptor::getServiceComponent,
                        subscriptionDescriptor -> subscriptionDescriptor,
                        (subscriptionDescriptor, subscriptionDescriptor2) -> {
                            throw new RegistryException("Duplicate subscription descriptor for service component: " + subscriptionDescriptor.getServiceComponent());
                        })
                );
    }

    public Optional<SubscriptionDescriptor> getSubscriptionDescriptorFor(final String serviceName) {
        return ofNullable(registry.get(serviceName));
    }

    public Subscription getSubscription(final String subscriptionName) {

        return registry.values().stream()
                .map(SubscriptionDescriptor::getSubscriptions)
                .flatMap(Collection::stream)
                .filter(subscription -> subscription.getName().equals(subscriptionName))
                .findFirst()
                .orElseThrow(() -> new RegistryException(format("Failed to find subscription '%s' in registry", subscriptionName)));
    }
}
