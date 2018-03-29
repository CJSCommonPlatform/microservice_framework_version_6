package uk.gov.justice.services.event.sourcing.subscription;

import static java.util.Optional.ofNullable;

import uk.gov.justice.subscription.domain.SubscriptionDescriptor;

import java.util.Map;
import java.util.Optional;

public class SubscriptionDescriptorRegistry {

    private final Map<String, SubscriptionDescriptor> registry;

    public SubscriptionDescriptorRegistry(final Map<String, SubscriptionDescriptor> registry) {
        this.registry = registry;
    }

    public Optional<SubscriptionDescriptor> getSubscriptionFor(final String serviceName) {
        return ofNullable(registry.get(serviceName));
    }
}
