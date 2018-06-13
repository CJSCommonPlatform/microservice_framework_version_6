package uk.gov.justice.services.event.sourcing.subscription;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.subscription.SubscriptionManager;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import org.slf4j.Logger;

public class DefaultSubscriptionManager implements SubscriptionManager {
    protected Logger logger = getLogger(DefaultSubscriptionManager.class);
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
        interceptorChainProcessor.process(interceptorContextWithInput(jsonEnvelope));
    }

    @Override
    public void startSubscription() {
        logger.debug(format("Starting subscription: %s for event source: %s", subscription.getName(), subscription.getEventSourceName()));
    }
}