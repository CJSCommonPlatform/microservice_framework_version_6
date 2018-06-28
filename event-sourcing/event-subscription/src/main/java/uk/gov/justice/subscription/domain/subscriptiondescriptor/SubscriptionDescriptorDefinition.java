package uk.gov.justice.subscription.domain.subscriptiondescriptor;

import java.util.List;
import java.util.Objects;

public class SubscriptionDescriptorDefinition {

    private final String specVersion;
    private final String service;
    private final String serviceComponent;
    private final List<Subscription> subscriptions;

    public SubscriptionDescriptorDefinition(final String specVersion, final String service, final String serviceComponent, final List<Subscription> subscriptions) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionDescriptorDefinition that = (SubscriptionDescriptorDefinition) o;
        return Objects.equals(specVersion, that.specVersion) &&
                Objects.equals(service, that.service) &&
                Objects.equals(serviceComponent, that.serviceComponent) &&
                Objects.equals(subscriptions, that.subscriptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(specVersion, service, serviceComponent, subscriptions);
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

}
