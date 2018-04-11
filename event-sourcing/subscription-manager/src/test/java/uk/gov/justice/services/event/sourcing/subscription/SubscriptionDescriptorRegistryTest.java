package uk.gov.justice.services.event.sourcing.subscription;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_MAP;
import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;

import java.util.Map;
import java.util.Optional;

import org.junit.Test;

public class SubscriptionDescriptorRegistryTest {

    @Test
    public void shouldMaintainARegistryOfServiceNamesToSubscriptionDescriptions() throws Exception {

        final String service_1 = "service_1";
        final String service_2 = "service_2";

        final SubscriptionDescriptor subscriptionDescriptor_1 = mock(SubscriptionDescriptor.class);
        final SubscriptionDescriptor subscriptionDescriptor_2 = mock(SubscriptionDescriptor.class);

        when(subscriptionDescriptor_1.getService()).thenReturn(service_1);
        when(subscriptionDescriptor_2.getService()).thenReturn(service_2);

        final Map<String, SubscriptionDescriptor> registry = of(
                service_1, subscriptionDescriptor_1,
                service_2, subscriptionDescriptor_2
        );

        final SubscriptionDescriptorRegistry subscriptionDescriptorRegistry = new SubscriptionDescriptorRegistry(registry);

        assertThat(subscriptionDescriptorRegistry.getSubscriptionDescriptorFor(service_1), is(Optional.of(subscriptionDescriptor_1)));
        assertThat(subscriptionDescriptorRegistry.getSubscriptionDescriptorFor(service_2), is(Optional.of(subscriptionDescriptor_2)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnEmptyIfNoSubsctriptionIsFoundForASpecifiedServiceName() throws Exception {

        final SubscriptionDescriptorRegistry subscriptionDescriptorRegistry = new SubscriptionDescriptorRegistry(EMPTY_MAP);

        assertThat(subscriptionDescriptorRegistry.getSubscriptionDescriptorFor("some-non-existent-service"), is(empty()));
    }

    @Test
    public void shouldGetASubscriptionByNameBySearchingAllASubscriptionDescriptors() throws Exception {

        final Subscription subscription_1_1 = mock(Subscription.class);
        final Subscription subscription_1_2 = mock(Subscription.class);
        final Subscription subscription_2_1 = mock(Subscription.class);
        final Subscription subscription_2_2 = mock(Subscription.class);

        final SubscriptionDescriptor subscriptionDescriptor_1 = mock(SubscriptionDescriptor.class);
        final SubscriptionDescriptor subscriptionDescriptor_2 = mock(SubscriptionDescriptor.class);

        when(subscriptionDescriptor_1.getSubscriptions()).thenReturn(asList(subscription_1_1, subscription_1_2));
        when(subscriptionDescriptor_2.getSubscriptions()).thenReturn(asList(subscription_2_1, subscription_2_2));

        when(subscription_1_1.getName()).thenReturn("subscription_1_1");
        when(subscription_1_2.getName()).thenReturn("subscription_1_2");
        when(subscription_2_1.getName()).thenReturn("subscription_2_1");
        when(subscription_2_2.getName()).thenReturn("subscription_2_2");

        final Map<String, SubscriptionDescriptor> registry = of(
                "service_1", subscriptionDescriptor_1,
                "service_2", subscriptionDescriptor_2
        );

        final SubscriptionDescriptorRegistry subscriptionDescriptorRegistry = new SubscriptionDescriptorRegistry(registry);

        assertThat(subscriptionDescriptorRegistry.getSubscription("subscription_2_1"), is(subscription_2_1));
    }

    @Test
    public void shouldThrowASubscriptioManagerProducerExceptionIfNoSubscriptionCanBeFoundWithTheSpecifiedName() throws Exception {

        final String thisSubscriptionDoesNotExist = "thisSubscriptionDoesNotExist";

        final Subscription subscription_1_1 = mock(Subscription.class);
        final Subscription subscription_1_2 = mock(Subscription.class);
        final Subscription subscription_2_1 = mock(Subscription.class);
        final Subscription subscription_2_2 = mock(Subscription.class);

        final SubscriptionDescriptor subscriptionDescriptor_1 = mock(SubscriptionDescriptor.class);
        final SubscriptionDescriptor subscriptionDescriptor_2 = mock(SubscriptionDescriptor.class);

        when(subscriptionDescriptor_1.getSubscriptions()).thenReturn(asList(subscription_1_1, subscription_1_2));
        when(subscriptionDescriptor_2.getSubscriptions()).thenReturn(asList(subscription_2_1, subscription_2_2));

        when(subscription_1_1.getName()).thenReturn("subscription_1_1");
        when(subscription_1_2.getName()).thenReturn("subscription_1_2");
        when(subscription_2_1.getName()).thenReturn("subscription_2_1");
        when(subscription_2_2.getName()).thenReturn("subscription_2_2");

        final Map<String, SubscriptionDescriptor> registry = of(
                "service_1", subscriptionDescriptor_1,
                "service_2", subscriptionDescriptor_2
        );

        final SubscriptionDescriptorRegistry subscriptionDescriptorRegistry = new SubscriptionDescriptorRegistry(registry);

        try {
            subscriptionDescriptorRegistry.getSubscription(thisSubscriptionDoesNotExist);
            fail();
        } catch (final SubscriptionManagerProducerException expected) {
            assertThat(expected.getMessage(), is("Failed to find subscription 'thisSubscriptionDoesNotExist' in registry"));
        }
    }
}
