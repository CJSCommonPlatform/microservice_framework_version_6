package uk.gov.justice.services.event.sourcing.subscription;

import uk.gov.justice.services.core.cdi.QualifierAnnotationExtractor;
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

@ApplicationScoped
public class SubscriptionManagerProducer {

    @Inject
    @Any
    Instance<EventSource> eventSourceInstance;

    @Inject
    SubscriptionDescriptorDefinitionRegistry subscriptionDescriptorRegistry;

    @Inject
    InterceptorChainProcessorProducer interceptorChainProcessorProducer;

    @Inject
    QualifierAnnotationExtractor qualifierAnnotationExtractor;

    @Produces
    @SubscriptionName
    public SubscriptionManager subscriptionManager(final InjectionPoint injectionPoint) {

        final SubscriptionName subscriptionName = qualifierAnnotationExtractor.getFrom(injectionPoint, SubscriptionName.class);
        final Subscription subscription = subscriptionDescriptorRegistry.getSubscriptionFor(subscriptionName.value());
        final EventSourceNameQualifier eventSourceNameQualifier = new EventSourceNameQualifier(subscription.getEventSourceName());

        return new DefaultSubscriptionManager(
                subscription,
                eventSourceInstance.select(eventSourceNameQualifier).get(),
                interceptorChainProcessorProducer.produceProcessor(injectionPoint));
    }
}
