package uk.gov.justice.subscription.domain.subscriptiondescriptor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class SubscriptionsDescriptorTest {
    @Test
    public void shouldReturnFalseForDifferentSubscriptionNameQualifiers() {
        final String specVersion1 = "1.0";
        final String service1 = "service1";
        final String serviceComponent1 = "EVENT_LISTENER";
        final List<Subscription> subscriptions1 = new ArrayList<>();

        final String specVersion2 = "2.0";
        final String service2 = "service2";
        final String serviceComponent2 = "EVENT_PROCESSOR";
        final List<Subscription> subscriptions2 = new ArrayList<>();


        final SubscriptionsDescriptor subscriptionsDescriptor1 = new SubscriptionsDescriptor(specVersion1, service1, serviceComponent1, 1, subscriptions1);
        final SubscriptionsDescriptor subscriptionsDescriptor2 = new SubscriptionsDescriptor(specVersion2, service2, serviceComponent2, 1, subscriptions2);
        assertFalse(subscriptionsDescriptor1.equals(subscriptionsDescriptor2));
    }

    @Test
    public void shouldReturnTrueForSameSubscriptionNameQualifiers() {

        final String specVersion = "1.0";
        final String service = "service";
        final String serviceComponent = "EVENT_LISTENER";
        final int prioritisation = 1;
        final List<Subscription> subscriptions = new ArrayList<>();

        final SubscriptionsDescriptor subscriptionsDescriptor1 = new SubscriptionsDescriptor(specVersion, service, serviceComponent, prioritisation, subscriptions);
        final SubscriptionsDescriptor subscriptionsDescriptor2 = new SubscriptionsDescriptor(specVersion, service, serviceComponent, prioritisation, subscriptions);
        assertTrue(subscriptionsDescriptor1.equals(subscriptionsDescriptor2));
    }
}
