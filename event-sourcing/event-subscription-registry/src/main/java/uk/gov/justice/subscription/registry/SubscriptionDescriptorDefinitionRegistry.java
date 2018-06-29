package uk.gov.justice.subscription.registry;

import static java.lang.String.format;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDefinition;

import java.util.Collection;
import java.util.Set;
/**
 * Registry containing {@link SubscriptionDescriptorDefinition}s set
 */
public class SubscriptionDescriptorDefinitionRegistry {
    private final Set<SubscriptionDescriptorDefinition> registry;

    public SubscriptionDescriptorDefinitionRegistry(final Set<SubscriptionDescriptorDefinition> subscriptionDescriptorDefinitions) {
        this.registry = subscriptionDescriptorDefinitions;
    }
    /**
     * Return a {@link Subscription} mapped to a subscription name
     *
     * @param subscriptionName the subscription name to look up
     * @return {@link Subscription}
     */
    public Subscription getSubscriptionFor(final String subscriptionName) {
        return registry.stream()
                .map(SubscriptionDescriptorDefinition::getSubscriptions)
                .flatMap(Collection::stream)
                .filter(subscription -> subscription.getName().equals(subscriptionName))
                .findFirst()
                .orElseThrow(() -> new RegistryException(format("Failed to find subscription '%s' in registry", subscriptionName)));
    }

    /**
     * Return a subscription component name
     *
     * @param subscriptionName the subscription name to look up
     * @return subscription component name
     */
    public String findComponentNameBy(final String subscriptionName) {
        final SubscriptionDescriptorDefinition first = registry
                .stream()
                .filter(subscriptionDescriptorDefinition -> isSubscriptionNameExist(subscriptionName, subscriptionDescriptorDefinition))
                .findFirst()
                .orElseThrow(() -> new RegistryException(format("Failed to find service component name in registry for subscription '%s' ", subscriptionName)));
        return first.getServiceComponent();
    }

    private boolean isSubscriptionNameExist(final String subscriptionName, final SubscriptionDescriptorDefinition subscriptionDescriptorDefinition) {
        return subscriptionDescriptorDefinition
                .getSubscriptions()
                .stream()
                .anyMatch(subscription1 -> subscription1.getName().equals(subscriptionName));
    }

    public Set<SubscriptionDescriptorDefinition> subscriptionDescriptorDefinitions() {
        return registry;
    }
}
