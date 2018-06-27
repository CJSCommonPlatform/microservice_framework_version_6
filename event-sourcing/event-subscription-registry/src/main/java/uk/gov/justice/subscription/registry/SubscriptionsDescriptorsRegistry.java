package uk.gov.justice.subscription.registry;

import static java.lang.String.format;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.util.Collection;
import java.util.Set;
/**
 * Registry containing {@link SubscriptionsDescriptor}s set
 */
public class SubscriptionsDescriptorsRegistry {
    private final Set<SubscriptionsDescriptor> registry;

    public SubscriptionsDescriptorsRegistry(final Set<SubscriptionsDescriptor> subscriptionsDescriptors) {
        this.registry = subscriptionsDescriptors;
    }
    /**
     * Return a {@link Subscription} mapped to a subscription name
     *
     * @param subscriptionName the subscription name to look up
     * @return {@link Subscription}
     */
    public Subscription getSubscriptionFor(final String subscriptionName) {
        return registry.stream()
                .map(SubscriptionsDescriptor::getSubscriptions)
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
        final SubscriptionsDescriptor first = registry
                .stream()
                .filter(subscriptionDescriptorDefinition -> isSubscriptionNameExist(subscriptionName, subscriptionDescriptorDefinition))
                .findFirst()
                .orElseThrow(() -> new RegistryException(format("Failed to find service component name in registry for subscription '%s' ", subscriptionName)));
        return first.getServiceComponent();
    }

    private boolean isSubscriptionNameExist(final String subscriptionName, final SubscriptionsDescriptor subscriptionsDescriptor) {
        return subscriptionsDescriptor
                .getSubscriptions()
                .stream()
                .anyMatch(subscription -> subscription.getName().equals(subscriptionName));
    }

    public Set<SubscriptionsDescriptor> subscriptionsDescriptors() {
        return registry;
    }
}
