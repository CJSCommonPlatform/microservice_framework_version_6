package uk.gov.justice.services.event.sourcing.subscription.manager;

import static java.lang.String.format;
import static uk.gov.justice.services.core.interceptor.InterceptorContext.interceptorContextWithInput;

import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorContext;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.subscription.SubscriptionManager;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;

public class DefaultSubscriptionManager implements SubscriptionManager {

    private final Subscription subscription;
    private final EventSource eventSource;
    private final InterceptorChainProcessor interceptorChainProcessor;
    private final Optional<EventBufferService> eventBufferService;
    private final Logger logger;

    public DefaultSubscriptionManager(final Subscription subscription,
                                      final EventSource eventSource,
                                      final InterceptorChainProcessor interceptorChainProcessor,
                                      final Optional<EventBufferService> eventBufferService,
                                      final Logger logger) {
        this.subscription = subscription;
        this.eventSource = eventSource;
        this.interceptorChainProcessor = interceptorChainProcessor;
        this.eventBufferService = eventBufferService;
        this.logger = logger;
    }

    @Override
    public void process(final JsonEnvelope incomingJsonEnvelope) {

        if (eventBufferService.isPresent()) {
            processWithEventBuffer(incomingJsonEnvelope);
        } else {
            processWithInterceptorChain(incomingJsonEnvelope);
        }
    }

    @Override
    public void startSubscription() {
        logger.debug(format("Starting subscription: %s for event source: %s", subscription.getName(), eventSource));
    }

    private void processWithEventBuffer(final JsonEnvelope incomingJsonEnvelope) {
        try (final Stream<JsonEnvelope> jsonEnvelopeStream = eventBufferService.get().currentOrderedEventsWith(incomingJsonEnvelope)) {
            jsonEnvelopeStream.forEach(this::processWithInterceptorChain);
        }
    }

    private void processWithInterceptorChain(final JsonEnvelope incomingJsonEnvelope) {
        final InterceptorContext interceptorContext = interceptorContextWithInput(incomingJsonEnvelope);
        interceptorChainProcessor.process(interceptorContext);
    }
}
