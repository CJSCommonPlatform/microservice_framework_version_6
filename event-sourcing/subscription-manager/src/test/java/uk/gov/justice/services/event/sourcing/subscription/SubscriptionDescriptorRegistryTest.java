package uk.gov.justice.services.event.sourcing.subscription;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.EMPTY_MAP;
import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.subscription.domain.SubscriptionDescriptor;

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

        assertThat(subscriptionDescriptorRegistry.getSubscriptionFor(service_1), is(Optional.of(subscriptionDescriptor_1)));
        assertThat(subscriptionDescriptorRegistry.getSubscriptionFor(service_2), is(Optional.of(subscriptionDescriptor_2)));
    }

    @Test
    public void shouldReturnEmptyIfNoSubsctriptionIsFoundForASpecifiedServiceName() throws Exception {

        final SubscriptionDescriptorRegistry subscriptionDescriptorRegistry = new SubscriptionDescriptorRegistry(EMPTY_MAP);

        assertThat(subscriptionDescriptorRegistry.getSubscriptionFor("some-non-existent-service"), is(empty()));
    }
}
