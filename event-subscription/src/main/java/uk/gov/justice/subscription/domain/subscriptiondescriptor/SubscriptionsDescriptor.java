package uk.gov.justice.subscription.domain.subscriptiondescriptor;

import java.util.List;
import java.util.Objects;

public class SubscriptionsDescriptor {

    private final String specVersion;
    private final String service;
    private final String serviceComponent;
    private final int prioritisation;
    private final List<Subscription> subscriptions;

    public SubscriptionsDescriptor(final String specVersion,
                                   final String service,
                                   final String serviceComponent,
                                   final int prioritisation,
                                   final List<Subscription> subscriptions) {
        this.specVersion = specVersion;
        this.service = service;
        this.serviceComponent = serviceComponent;
        this.prioritisation = prioritisation;
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

    public int getPrioritisation() {
        return prioritisation;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionsDescriptor)) return false;
        final SubscriptionsDescriptor that = (SubscriptionsDescriptor) o;
        return Objects.equals(specVersion, that.specVersion) &&
                Objects.equals(service, that.service) &&
                Objects.equals(serviceComponent, that.serviceComponent) &&
                Objects.equals(prioritisation, that.prioritisation) &&
                Objects.equals(subscriptions, that.subscriptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(specVersion, service, serviceComponent, prioritisation, subscriptions);
    }

    @Override
    public String toString() {
        return "SubscriptionsDescriptor{" +
                "specVersion='" + specVersion + '\'' +
                ", service='" + service + '\'' +
                ", serviceComponent='" + serviceComponent + '\'' +
                ", prioritisation='" + prioritisation + '\'' +
                ", subscriptions=" + subscriptions +
                '}';
    }
}
