package uk.gov.justice.services.event.sourcing.subscription;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.subscription.SubscriptionManager;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDefinition;
import uk.gov.justice.subscription.registry.SubscriptionDescriptorDefinitionRegistry;

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

@Startup
@Singleton
public class SubscriptionManagerStartUp {

    @Inject
    @Any
    private Instance<EventSource> eventSourceInstance;

    @Inject
    private SubscriptionDescriptorDefinitionRegistry subscriptionDescriptorDefinitionRegistry;

    @Inject
    private EventSource eventSource;

    @Inject
    private InterceptorChainProcessor interceptorChainProcessor;


    @PostConstruct
    public void startSubscriptionSequenceSubscriptionManagerStartUp() {
        final Stream<SubscriptionDescriptorDefinition> subscriptionDescriptorDefinitions =
                subscriptionDescriptorDefinitionRegistry.subscriptionDescriptorDefinitions();

        subscriptionDescriptorDefinitions.forEach(
                subscriptionDescriptorDefinition ->
                {
                    final List<Subscription> subscriptions = subscriptionDescriptorDefinition.getSubscriptions();
                    subscriptions.forEach(subscription -> {
                        final EventSourceNameQualifier eventSourceName = new EventSourceNameQualifier(subscription.getEventSourceName());
                        final EventSource eventSource = eventSourceInstance.select(eventSourceName).get();
                        final SubscriptionManager subscriptionManager = new DefaultSubscriptionManager(subscription, eventSource,
                                interceptorChainProcessor);
                        subscriptionManager.startSubscription();
                    });
                });
    }


}
