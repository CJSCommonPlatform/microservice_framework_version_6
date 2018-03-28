package uk.gov.justice.subscription.domain.builders;

import uk.gov.justice.subscription.domain.Subscription;
import uk.gov.justice.subscription.domain.SubscriptionDescriptor;

import java.util.ArrayList;
import java.util.List;

public final class SubscriptionDescriptorBuilder {
    
    private String specVersion;
    private String service;
    private String serviceComponent;
    private final List<Subscription> subscriptions = new ArrayList<>();

    private SubscriptionDescriptorBuilder() {
    }

    public static SubscriptionDescriptorBuilder subscriptionDescriptor() {
        return new SubscriptionDescriptorBuilder();
    }

    public SubscriptionDescriptorBuilder withSpecVersion(final String specVersion) {
        this.specVersion = specVersion;
        return this;
    }

    public SubscriptionDescriptorBuilder withService(final String service) {
        this.service = service;
        return this;
    }

    public SubscriptionDescriptorBuilder withServiceComponent(final String serviceComponent) {
        this.serviceComponent = serviceComponent;
        return this;
    }

    public SubscriptionDescriptorBuilder withSubscription(final Subscription subscription) {
        this.subscriptions.add(subscription);
        return this;
    }

    public SubscriptionDescriptorBuilder withSubscriptions(final List<Subscription> subscriptions) {
        this.subscriptions.addAll(subscriptions);
        return this;
    }

    public SubscriptionDescriptor build() {
        return new SubscriptionDescriptor(specVersion, service, serviceComponent, subscriptions);
    }
}
