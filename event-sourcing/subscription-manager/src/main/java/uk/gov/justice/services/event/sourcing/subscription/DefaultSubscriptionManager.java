package uk.gov.justice.services.event.sourcing.subscription;

import static uk.gov.justice.services.core.interceptor.DefaultInterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.subscription.SubscriptionManager;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

public class DefaultSubscriptionManager implements SubscriptionManager {

    private final Subscription subscription;
    private final EventSource eventSource;
    private final InterceptorChainProcessor interceptorChainProcessor;

    public DefaultSubscriptionManager(final Subscription subscription,
                                      final EventSource eventSource,
                                      final InterceptorChainProcessor interceptorChainProcessor) {
        this.subscription = subscription;
        this.eventSource = eventSource;
        this.interceptorChainProcessor = interceptorChainProcessor;
    }

    @Override
    public void process(final JsonEnvelope jsonEnvelope) {
        //Start getting all events
        interceptorChainProcessor.process(interceptorContextWithInput(jsonEnvelope));
    }

    @Override
    public void startSubscription() {
        System.out.println("Subscription Process started");
    }
}
