package uk.gov.justice.services.event.sourcing.subscription;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.core.cdi.QualifierAnnotationExtractor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.subscription.SubscriptionManager;
import uk.gov.justice.services.subscription.annotation.SubscriptionName;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.registry.SubscriptionDescriptorDefinitionRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
public class SubscriptionManagerProducer {

    private final Map<String, SubscriptionManager> subscriptionManagerMap = new ConcurrentHashMap<>();

    @Inject
    Logger logger;

    @Inject
    @Any
    private Instance<EventSource> eventSourceInstance;

    @Inject
    private InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Inject
    private SubscriptionDescriptorDefinitionRegistry subscriptionDescriptorRegistry;

    @Inject
    private QualifierAnnotationExtractor qualifierAnnotationExtractor;

    @Inject
    EventReplayer eventReplayer;

    @Produces
    @SubscriptionName
    public SubscriptionManager subscriptionManager(final InjectionPoint injectionPoint) {
        final SubscriptionName subscriptionName = qualifierAnnotationExtractor.getFrom(injectionPoint, SubscriptionName.class);
        logger.debug(format("Creating subscription manager for subscription name: %s", subscriptionName.value()));

        final Subscription subscription = subscriptionDescriptorRegistry.getSubscriptionFor(subscriptionName.value());

        return subscriptionManagerMap.computeIfAbsent(subscription.getName(), k -> create(subscription));
    }

    private SubscriptionManager create(final Subscription subscription) {
        logger.debug(format("Retrieving from subscription manager map : %s", subscription.getName()));
        final String componentName = subscriptionDescriptorRegistry.findComponentNameBy(subscription.getName());
        final InterceptorChainProcessor interceptorChainProcessor = interceptorChainProcessorProducer.produceProcessor(componentName);

        return new DefaultSubscriptionManager(
                subscription,
                getEventSource(subscription.getEventSourceName()),
                interceptorChainProcessor,
                eventReplayer,
                getLogger(SubscriptionManager.class));
    }

    private EventSource getEventSource(final String eventSourceName) {
        final EventSourceNameQualifier eventSourceNameQualifier = new EventSourceNameQualifier(eventSourceName);
        return eventSourceInstance.select(eventSourceNameQualifier).get();
    }
}



