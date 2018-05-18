package uk.gov.justice.subscription.registry;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDefinition;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/**
 * Registry containing {@link SubscriptionDescriptorDefinition}s mapped by the serviceComponentName
 */
public class SubscriptionDescriptorDefinitionRegistry {

    private final Map<String, SubscriptionDescriptorDefinition> registry;

    private final BinaryOperator<SubscriptionDescriptorDefinition> throwRegistryExceptionWhenDuplicate =
            (subscriptionDescriptor, subscriptionDescriptor2) -> {
                throw new RegistryException("Duplicate subscription descriptor for service component: " + subscriptionDescriptor.getServiceComponent());
            };

    public SubscriptionDescriptorDefinitionRegistry(final Stream<SubscriptionDescriptorDefinition> subscriptionDescriptorDefinitions) {
        this.registry = subscriptionDescriptorDefinitions
                .collect(toMap(
                        SubscriptionDescriptorDefinition::getServiceComponent,
                        subscriptionDescriptorDefinition -> subscriptionDescriptorDefinition,
                        throwRegistryExceptionWhenDuplicate)
                );
    }

    /**
     * Return a {@link SubscriptionDescriptorDefinition} mapped to a subscription component name or
     * empty if not mapped.
     *
     * @param serviceComponentName the subscription component name to look up
     * @return Optional of {@link SubscriptionDescriptorDefinition} or empty
     */
    public Optional<SubscriptionDescriptorDefinition> getSubscriptionDescriptorDescriptorFor(final String serviceComponentName) {
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
                .map(SubscriptionDescriptorDefinition::getSubscriptions)
                .flatMap(Collection::stream)
                .filter(subscription -> subscription.getName().equals(subscriptionName))
                .findFirst()
                .orElseThrow(() -> new RegistryException(format("Failed to find subscription '%s' in registry", subscriptionName)));
    }
}
