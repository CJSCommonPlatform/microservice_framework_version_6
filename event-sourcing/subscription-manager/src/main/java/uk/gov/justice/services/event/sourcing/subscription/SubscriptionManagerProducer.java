package uk.gov.justice.services.event.sourcing.subscription;

import static java.lang.String.format;

import uk.gov.justice.services.core.cdi.SubscriptionName;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.subscription.domain.Subscription;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

@ApplicationScoped
public class SubscriptionManagerProducer {

    @Inject
    Instance<EventSource> eventsourceInstance;

    @Inject
    SubscriptionDescriptorRegistry subscriptionDescriptorRegistry;

    @Inject
    SubscriptionNameAnnotationExtractor subscriptionNameAnnotationExtractor;

    @Produces
    public SubscriptionManager subscriptionManager(final InjectionPoint injectionPoint) {

        final SubscriptionName subscriptionName = subscriptionNameAnnotationExtractor.getFrom(injectionPoint);

        final Instance<EventSource> eventSourceInstance = eventsourceInstance.select(subscriptionName);

        if(eventSourceInstance != null) {
            final EventSource eventSource = eventSourceInstance.get();
            final Subscription subscription = subscriptionDescriptorRegistry.getSubscription(subscriptionName.value());

            return new SubscriptionManager(subscription, eventSource);
        }

        throw new SubscriptionManagerProducerException(format("Failed to find instance of event souce with Qualifier '%s'", subscriptionName.value()));
    }
}
