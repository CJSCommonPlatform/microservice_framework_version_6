package uk.gov.justice.subscription.domain.builders;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.subscription.domain.builders.SubscriptionsDescriptorBuilder.subscriptionsDescriptor;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import org.junit.Test;

public class SubscriptionsDescriptorBuilderTest {

    @Test
    public void shouldBuildASubscription() {

        final String specVersion = "specVersion";
        final String service = "service";
        final String serviceComponent = "serviceComponent";
        final int prioritisation = 1;
        final Subscription subscription_1 = mock(Subscription.class);
        final Subscription subscription_2 = mock(Subscription.class);

        final SubscriptionsDescriptor subscriptionsDescriptor = subscriptionsDescriptor()
                .withSpecVersion(specVersion)
                .withService(service)
                .withServiceComponent(serviceComponent)
                .withPrioritisation(prioritisation)
                .withSubscriptions(asList(subscription_1, subscription_2))
                .build();

        assertThat(subscriptionsDescriptor.getSpecVersion(), is(specVersion));
        assertThat(subscriptionsDescriptor.getService(), is(service));
        assertThat(subscriptionsDescriptor.getServiceComponent(), is(serviceComponent));
        assertThat(subscriptionsDescriptor.getPrioritisation(), is(prioritisation));
        assertThat(subscriptionsDescriptor.getSubscriptions(), hasItem(subscription_1));
        assertThat(subscriptionsDescriptor.getSubscriptions(), hasItem(subscription_2));
    }

    @Test
    public void shouldBeAbleToAddSubscriptionsOneAtATime() {

        final String specVersion = "specVersion";
        final String service = "service";
        final String serviceComponent = "serviceComponent";
        final int prioritisation = 1;
        final Subscription subscription_1 = mock(Subscription.class);
        final Subscription subscription_2 = mock(Subscription.class);

        final SubscriptionsDescriptor subscriptionsDescriptor = subscriptionsDescriptor()
                .withSpecVersion(specVersion)
                .withService(service)
                .withServiceComponent(serviceComponent)
                .withPrioritisation(prioritisation)
                .withSubscription(subscription_1)
                .withSubscription(subscription_2)
                .build();

        assertThat(subscriptionsDescriptor.getSpecVersion(), is(specVersion));
        assertThat(subscriptionsDescriptor.getService(), is(service));
        assertThat(subscriptionsDescriptor.getServiceComponent(), is(serviceComponent));
        assertThat(subscriptionsDescriptor.getPrioritisation(), is(prioritisation));
        assertThat(subscriptionsDescriptor.getSubscriptions(), hasItem(subscription_1));
        assertThat(subscriptionsDescriptor.getSubscriptions(), hasItem(subscription_2));
    }
}
