package uk.gov.justice.services.event.sourcing.subscription.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.cdi.QualifierAnnotationExtractor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.subscription.SubscriptionManager;
import uk.gov.justice.services.subscription.annotation.SubscriptionName;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.registry.SubscriptionDescriptorDefinitionRegistry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
@ApplicationScoped
public class SubscriptionManagerProducer {
    @Inject
    Logger LOGGER = LoggerFactory.getLogger(SubscriptionManagerProducer.class);

    Map<String, SubscriptionManager> subscriptionManagerMap = new ConcurrentHashMap<>();

    @Inject
    @Any
    Instance<EventSource> eventSourceInstance;

    @Inject
    InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Inject
    SubscriptionDescriptorDefinitionRegistry subscriptionDescriptorRegistry;

    @Inject
    QualifierAnnotationExtractor qualifierAnnotationExtractor;

    @Produces
    @SubscriptionName
    public SubscriptionManager subscriptionManager(final InjectionPoint injectionPoint) {
        final SubscriptionName subscriptionName = qualifierAnnotationExtractor.getFrom(injectionPoint, SubscriptionName.class);
        LOGGER.debug(format("Creating subscription manager for subscription name: %s", subscriptionName.value()));

        final Subscription subscription = subscriptionDescriptorRegistry.getSubscriptionFor(subscriptionName.value());

        return subscriptionManagerMap.computeIfAbsent(subscription.getName(), k -> create(subscription));
    }

    private SubscriptionManager create(final Subscription subscription) {
        LOGGER.debug(format("Retrieving from subscription manager map : %s", subscription.getName()));
        final String componentName = subscriptionDescriptorRegistry.findComponentNameBy(subscription.getName());
        final InterceptorChainProcessor interceptorChainProcessor = interceptorChainProcessorProducer.produceLocalProcessor(componentName);
        return new DefaultSubscriptionManager(subscription, getEventSource(subscription.getEventSourceName()), interceptorChainProcessor);
    }

    private EventSource getEventSource(final String eventSourceName) {
        final EventSourceNameQualifier eventSourceNameQualifier = new EventSourceNameQualifier(eventSourceName);
        return eventSourceInstance.select(eventSourceNameQualifier).get();
    }
}



