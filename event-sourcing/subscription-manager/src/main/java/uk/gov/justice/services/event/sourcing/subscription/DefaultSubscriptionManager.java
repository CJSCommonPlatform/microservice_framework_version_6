package uk.gov.justice.services.event.sourcing.subscription;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.subscription.SubscriptionManager;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.UUID;

import org.slf4j.Logger;

public class DefaultSubscriptionManager implements SubscriptionManager {

    private final Subscription subscription;
    private final EventSource eventSource;
    private final InterceptorChainProcessor interceptorChainProcessor;
    private final EventReplayer eventReplayer;
    private final Logger logger;

    public DefaultSubscriptionManager(final Subscription subscription,
                                      final EventSource eventSource,
                                      final InterceptorChainProcessor interceptorChainProcessor,
                                      final EventReplayer eventReplayer,
                                      final Logger logger) {
        this.subscription = subscription;
        this.eventSource = eventSource;
        this.interceptorChainProcessor = interceptorChainProcessor;
        this.eventReplayer = eventReplayer;
        this.logger = logger;
    }

    @Override
    public void process(final JsonEnvelope jsonEnvelope) {
        interceptorChainProcessor.process(interceptorContextWithInput(jsonEnvelope));
    }

    @Override
    public void startSubscription() {
        logger.debug(format("Starting subscription: %s for event source: %s", subscription.getName(), subscription.getEventSourceName()));

        logger.info("------------ Starting to replay events -----------------");

        eventReplayer.replay(interceptorChainProcessor);

        logger.info("------------ Finished replay of events -----------------");
    }
}