package uk.gov.justice.subscription.registry;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/**
 * Registry containing {@link SubscriptionDescriptor}s mapped by the serviceComponentName
 */
public class SubscriptionDescriptorRegistry {

    private final Map<String, SubscriptionDescriptor> registry;

    private final BinaryOperator<SubscriptionDescriptor> throwRegistryExceptionWhenDuplicate =
            (subscriptionDescriptor, subscriptionDescriptor2) -> {
                throw new RegistryException("Duplicate subscription descriptor for service component: " + subscriptionDescriptor.getServiceComponent());
            };

    public SubscriptionDescriptorRegistry(final Stream<SubscriptionDescriptor> subscriptionDescriptors) {
        this.registry = subscriptionDescriptors
                .collect(toMap(
                        SubscriptionDescriptor::getServiceComponent,
                        subscriptionDescriptor -> subscriptionDescriptor,
                        throwRegistryExceptionWhenDuplicate)
                );
    }

    /**
     * Return a {@link SubscriptionDescriptor} mapped to a subscription component name or
     * empty if not mapped.
     *
     * @param serviceComponentName the subscription component name to look up
     * @return Optional of {@link SubscriptionDescriptor} or empty
     */
    public Optional<SubscriptionDescriptor> getSubscriptionDescriptorFor(final String serviceComponentName) {
        return ofNullable(registry.get(serviceComponentName));
    }

    /**
     * Return a {@link Subscription} mapped to a subscription name
     *
     * @param subscriptionName the subscription name to look up
     * @return {@link Subscription}
     */
    public Subscription getSubscriptionFor(final String subscriptionName) {
        return registry.values().stream()
                .map(SubscriptionDescriptor::getSubscriptions)
                .flatMap(Collection::stream)
                .filter(subscription -> subscription.getName().equals(subscriptionName))
                .findFirst()
                .orElseThrow(() -> new RegistryException(format("Failed to find subscription '%s' in registry", subscriptionName)));
    }
}
