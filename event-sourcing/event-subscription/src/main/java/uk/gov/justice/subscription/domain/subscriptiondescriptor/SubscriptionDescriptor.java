package uk.gov.justice.subscription.domain.subscriptiondescriptor;

import java.util.List;

public class SubscriptionDescriptor {

    private final String specVersion;
    private final String service;
    private final String serviceComponent;
    private final List<Subscription> subscriptions;

    public SubscriptionDescriptor(final String specVersion, final String service, final String serviceComponent, final List<Subscription> subscriptions) {
        this.specVersion = specVersion;
        this.service = service;
        this.serviceComponent = serviceComponent;
        this.subscriptions = subscriptions;
    }

    public String getSpecVersion() {
        return specVersion;
    }

    public String getService() {
        return service;
    }

    public String getServiceComponent() {
        return serviceComponent;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

}
