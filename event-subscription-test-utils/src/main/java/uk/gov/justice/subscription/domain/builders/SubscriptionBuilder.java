package uk.gov.justice.subscription.domain.builders;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Event;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;

import java.util.ArrayList;
import java.util.List;

public final class SubscriptionBuilder {

    private String name;
    private final List<Event> events = new ArrayList<>();
    private String eventSourceName;
    private int prioritisation;

    private SubscriptionBuilder() {
    }

    public static SubscriptionBuilder subscription() {
        return new SubscriptionBuilder();
    }

    public SubscriptionBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public SubscriptionBuilder withEvent(final Event event) {
        this.events.add(event);
        return this;
    }

    public SubscriptionBuilder withEvents(final List<Event> events) {
        this.events.addAll(events);
        return this;
    }

    public SubscriptionBuilder withEventSourceName(final String eventSourceName) {
        this.eventSourceName = eventSourceName;
        return this;
    }

    public SubscriptionBuilder withPrioritisation(final int prioritisation) {
        this.prioritisation = prioritisation;
        return this;
    }

    public Subscription build() {
        return new Subscription(name, events, eventSourceName, prioritisation);
    }
}
