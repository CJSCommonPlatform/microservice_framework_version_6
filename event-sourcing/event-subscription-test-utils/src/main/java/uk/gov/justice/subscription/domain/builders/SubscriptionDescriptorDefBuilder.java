package uk.gov.justice.subscription.domain.builders;

import uk.gov.justice.subscription.domain.SubscriptionDescriptor;
import uk.gov.justice.subscription.domain.SubscriptionDescriptorDef;

public final class SubscriptionDescriptorDefBuilder {
    
    private SubscriptionDescriptor subscriptionDescriptor;

    private SubscriptionDescriptorDefBuilder() {
    }

    public static SubscriptionDescriptorDefBuilder subscriptionDescriptorDef() {
        return new SubscriptionDescriptorDefBuilder();
    }

    public SubscriptionDescriptorDefBuilder withSubscriptionDescriptor(final SubscriptionDescriptor subscriptionDescriptor) {
        this.subscriptionDescriptor = subscriptionDescriptor;
        return this;
    }

    public SubscriptionDescriptorDef build() {
        return new SubscriptionDescriptorDef(subscriptionDescriptor);
    }
}
