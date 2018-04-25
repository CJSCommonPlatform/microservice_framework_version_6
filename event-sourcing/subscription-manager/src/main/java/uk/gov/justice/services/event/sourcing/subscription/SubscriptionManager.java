package uk.gov.justice.services.event.sourcing.subscription;

import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

public class SubscriptionManager {

    private final Subscription subscription;
    private final EventSource eventSource;

    public SubscriptionManager(final Subscription subscription, final EventSource eventSource) {
        this.subscription = subscription;
        this.eventSource = eventSource;
    }

    // Temporary until we decide what this class should do
    public Subscription getSubscription() {
        return subscription;
    }

    // Temporary until we decide what this class should do
    public EventSource getEventSource() {
        return eventSource;
    }
}
