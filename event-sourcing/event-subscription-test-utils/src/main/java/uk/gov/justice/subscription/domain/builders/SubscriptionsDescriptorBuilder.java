package uk.gov.justice.subscription.domain.builders;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.util.ArrayList;
import java.util.List;

public final class SubscriptionsDescriptorBuilder {
    
    private String specVersion;
    private String service;
    private String serviceComponent;
    private final List<Subscription> subscriptions = new ArrayList<>();

    private SubscriptionsDescriptorBuilder() {
    }

    public static SubscriptionsDescriptorBuilder subscriptionsDescriptor() {
        return new SubscriptionsDescriptorBuilder();
    }

    public SubscriptionsDescriptorBuilder withSpecVersion(final String specVersion) {
        this.specVersion = specVersion;
        return this;
    }

    public SubscriptionsDescriptorBuilder withService(final String service) {
        this.service = service;
        return this;
    }

    public SubscriptionsDescriptorBuilder withServiceComponent(final String serviceComponent) {
        this.serviceComponent = serviceComponent;
        return this;
    }

    public SubscriptionsDescriptorBuilder withSubscription(final Subscription subscription) {
        this.subscriptions.add(subscription);
        return this;
    }

    public SubscriptionsDescriptorBuilder withSubscriptions(final List<Subscription> subscriptions) {
        this.subscriptions.addAll(subscriptions);
        return this;
    }

    public SubscriptionsDescriptor build() {
        return new SubscriptionsDescriptor(specVersion, service, serviceComponent, subscriptions);
    }
}
