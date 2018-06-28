package uk.gov.justice.services.event.sourcing.subscription.manager;

import uk.gov.justice.services.subscription.SubscriptionManager;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDefinition;
import uk.gov.justice.subscription.registry.SubscriptionDescriptorDefinitionRegistry;

import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@Singleton
public class SubscriptionManagerStartUp {
    @Inject
    Logger LOGGER = LoggerFactory.getLogger(SubscriptionManagerStartUp.class);

    @Inject
    @Any
    Instance<SubscriptionManager> subscriptionManagers;

    @Inject
    SubscriptionDescriptorDefinitionRegistry subscriptionDescriptorDefinitionRegistry;

    @PostConstruct
    public void start() {
        LOGGER.info("SubscriptionManagerStartUp started");
        final Set<SubscriptionDescriptorDefinition> subscriptionDescriptorDefinitions =
                subscriptionDescriptorDefinitionRegistry.subscriptionDescriptorDefinitions();

        subscriptionDescriptorDefinitions.forEach(
                subscriptionDescriptorDefinition ->
                {
                    final List<Subscription> subscriptions = subscriptionDescriptorDefinition.getSubscriptions();
                    subscriptions.forEach(subscription -> {
                        final SubscriptionNameQualifier subscriptionNameQualifier = new SubscriptionNameQualifier(subscription.getName());
                        final SubscriptionManager subscriptionManager = subscriptionManagers.select(subscriptionNameQualifier).get();
                        subscriptionManager.startSubscription();
                    });
                });
    }
}
