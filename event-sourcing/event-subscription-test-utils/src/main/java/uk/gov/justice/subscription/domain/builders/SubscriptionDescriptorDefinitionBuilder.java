package uk.gov.justice.subscription.domain.builders;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDefinition;

import java.util.ArrayList;
import java.util.List;

public final class SubscriptionDescriptorDefinitionBuilder {
    
    private String specVersion;
    private String service;
    private String serviceComponent;
    private final List<Subscription> subscriptions = new ArrayList<>();

    private SubscriptionDescriptorDefinitionBuilder() {
    }

    public static SubscriptionDescriptorDefinitionBuilder subscriptionDescriptorDefinition() {
        return new SubscriptionDescriptorDefinitionBuilder();
    }

    public SubscriptionDescriptorDefinitionBuilder withSpecVersion(final String specVersion) {
        this.specVersion = specVersion;
        return this;
    }

    public SubscriptionDescriptorDefinitionBuilder withService(final String service) {
        this.service = service;
        return this;
    }

    public SubscriptionDescriptorDefinitionBuilder withServiceComponent(final String serviceComponent) {
        this.serviceComponent = serviceComponent;
        return this;
    }

    public SubscriptionDescriptorDefinitionBuilder withSubscription(final Subscription subscription) {
        this.subscriptions.add(subscription);
        return this;
    }

    public SubscriptionDescriptorDefinitionBuilder withSubscriptions(final List<Subscription> subscriptions) {
        this.subscriptions.addAll(subscriptions);
        return this;
    }

    public SubscriptionDescriptorDefinition build() {
        return new SubscriptionDescriptorDefinition(specVersion, service, serviceComponent, subscriptions);
    }
}
