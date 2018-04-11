package uk.gov.justice.subscription.domain.builders;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.subscription.domain.builders.SubscriptionDescriptorDefBuilder.subscriptionDescriptorDef;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDef;

import org.junit.Test;

public class SubscriptionDescriptorDefBuilderTest {

    @Test
    public void shouldBuildASubscriptionDescriptorDef() throws Exception {

        final SubscriptionDescriptor subscriptionDescriptor = mock(SubscriptionDescriptor.class);

        final SubscriptionDescriptorDef subscriptionDescriptorDef = subscriptionDescriptorDef()
                .withSubscriptionDescriptor(subscriptionDescriptor)
                .build();

        assertThat(subscriptionDescriptorDef.getSubscriptionDescriptor(), is(subscriptionDescriptor));
    }
}
