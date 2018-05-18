package uk.gov.justice.subscription.domain.builders;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.subscription.domain.builders.SubscriptionDescriptorDefinitionBuilder.subscriptionDescriptorDefinition;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDefinition;

import org.junit.Test;

public class SubscriptionDescriptorDefinitionBuilderTest {

    @Test
    public void shouldBuildASubscription() throws Exception {

        final String specVersion = "specVersion";
        final String service = "service";
        final String serviceComponent = "serviceComponent";
        final Subscription subscription_1 = mock(Subscription.class);
        final Subscription subscription_2 = mock(Subscription.class);

        final SubscriptionDescriptorDefinition subscriptionDescriptorDefinition = subscriptionDescriptorDefinition()
                .withSpecVersion(specVersion)
                .withService(service)
                .withServiceComponent(serviceComponent)
                .withSubscriptions(asList(subscription_1, subscription_2))
                .build();

        assertThat(subscriptionDescriptorDefinition.getSpecVersion(), is(specVersion));
        assertThat(subscriptionDescriptorDefinition.getService(), is(service));
        assertThat(subscriptionDescriptorDefinition.getServiceComponent(), is(serviceComponent));
        assertThat(subscriptionDescriptorDefinition.getSubscriptions(), hasItem(subscription_1));
        assertThat(subscriptionDescriptorDefinition.getSubscriptions(), hasItem(subscription_2));
    }

    @Test
    public void shouldBeAbleToAddSubscriptionsOneAtATime() throws Exception {

        final String specVersion = "specVersion";
        final String service = "service";
        final String serviceComponent = "serviceComponent";
        final Subscription subscription_1 = mock(Subscription.class);
        final Subscription subscription_2 = mock(Subscription.class);

        final SubscriptionDescriptorDefinition subscriptionDescriptorDefinition = subscriptionDescriptorDefinition()
                .withSpecVersion(specVersion)
                .withService(service)
                .withServiceComponent(serviceComponent)
                .withSubscription(subscription_1)
                .withSubscription(subscription_2)
                .build();

        assertThat(subscriptionDescriptorDefinition.getSpecVersion(), is(specVersion));
        assertThat(subscriptionDescriptorDefinition.getService(), is(service));
        assertThat(subscriptionDescriptorDefinition.getServiceComponent(), is(serviceComponent));
        assertThat(subscriptionDescriptorDefinition.getSubscriptions(), hasItem(subscription_1));
        assertThat(subscriptionDescriptorDefinition.getSubscriptions(), hasItem(subscription_2));
    }
}
