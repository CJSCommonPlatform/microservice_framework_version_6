package uk.gov.justice.services.event.sourcing.subscription;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.subscription.annotation.SubscriptionName;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.registry.SubscriptionDescriptorRegistry;

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
    Instance<EventSource> eventsourceInstance;

    @Inject
    SubscriptionDescriptorRegistry subscriptionDescriptorRegistry;

    @Inject
    QualifierAnnotationExtractor qualifierAnnotationExtractor;

    @Produces
    @SubscriptionName
    public SubscriptionManager subscriptionManager(final InjectionPoint injectionPoint) {

        final SubscriptionName subscriptionName = qualifierAnnotationExtractor.getFrom(injectionPoint, SubscriptionName.class);
        final Subscription subscription = subscriptionDescriptorRegistry.getSubscription(subscriptionName.value());

        final EventSourceNameQualifier eventSourceNameQualifier = new EventSourceNameQualifier(subscription.getEventSourceName());

        final Instance<EventSource> eventSourceInstance = eventsourceInstance.select(eventSourceNameQualifier);

        if (eventSourceInstance == null) {
            throw new SubscriptionManagerProducerException(format("Failed to find instance of event source with Qualifier '%s'", subscription.getEventSourceName()));
        }

        final EventSource eventSource = eventSourceInstance.get();
        return new SubscriptionManager(subscription, eventSource);
    }
}
