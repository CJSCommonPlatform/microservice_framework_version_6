package uk.gov.justice.subscription.domain.builders;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.subscription.domain.builders.SubscriptionDescriptorBuilder.subscriptionDescriptor;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;

import org.junit.Test;

public class SubscriptionDescriptorBuilderTest {

    @Test
    public void shouldBuildASubscription() throws Exception {

        final String specVersion = "specVersion";
        final String service = "service";
        final String serviceComponent = "serviceComponent";
        final Subscription subscription_1 = mock(Subscription.class);
        final Subscription subscription_2 = mock(Subscription.class);

        final SubscriptionDescriptor subscriptionDescriptor = subscriptionDescriptor()
                .withSpecVersion(specVersion)
                .withService(service)
                .withServiceComponent(serviceComponent)
                .withSubscriptions(asList(subscription_1, subscription_2))
                .build();

        assertThat(subscriptionDescriptor.getSpecVersion(), is(specVersion));
        assertThat(subscriptionDescriptor.getService(), is(service));
        assertThat(subscriptionDescriptor.getServiceComponent(), is(serviceComponent));
        assertThat(subscriptionDescriptor.getSubscriptions(), hasItem(subscription_1));
        assertThat(subscriptionDescriptor.getSubscriptions(), hasItem(subscription_2));
    }

    @Test
    public void shouldBeAbleToAddSubscriptionsOneAtATime() throws Exception {

        final String specVersion = "specVersion";
        final String service = "service";
        final String serviceComponent = "serviceComponent";
        final Subscription subscription_1 = mock(Subscription.class);
        final Subscription subscription_2 = mock(Subscription.class);

        final SubscriptionDescriptor subscriptionDescriptor = subscriptionDescriptor()
                .withSpecVersion(specVersion)
                .withService(service)
                .withServiceComponent(serviceComponent)
                .withSubscription(subscription_1)
                .withSubscription(subscription_2)
                .build();

        assertThat(subscriptionDescriptor.getSpecVersion(), is(specVersion));
        assertThat(subscriptionDescriptor.getService(), is(service));
        assertThat(subscriptionDescriptor.getServiceComponent(), is(serviceComponent));
        assertThat(subscriptionDescriptor.getSubscriptions(), hasItem(subscription_1));
        assertThat(subscriptionDescriptor.getSubscriptions(), hasItem(subscription_2));
    }
}
