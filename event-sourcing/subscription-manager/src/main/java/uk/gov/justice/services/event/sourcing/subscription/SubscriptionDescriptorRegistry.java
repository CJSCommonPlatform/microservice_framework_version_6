package uk.gov.justice.services.event.sourcing.subscription;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import uk.gov.justice.subscription.domain.Subscription;
import uk.gov.justice.subscription.domain.SubscriptionDescriptor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class SubscriptionDescriptorRegistry {

    private final Map<String, SubscriptionDescriptor> registry;

    public SubscriptionDescriptorRegistry(final Map<String, SubscriptionDescriptor> registry) {
        this.registry = registry;
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
                .orElseThrow(() -> new SubscriptionManagerProducerException(format("Failed to find subscription '%s' in registry", subscriptionName)));
    }
}
