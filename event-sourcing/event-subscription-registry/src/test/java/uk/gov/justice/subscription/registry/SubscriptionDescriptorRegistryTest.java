package uk.gov.justice.subscription.registry;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.subscription.domain.builders.SubscriptionBuilder.subscription;
import static uk.gov.justice.subscription.domain.builders.SubscriptionDescriptorBuilder.subscriptionDescriptor;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptor;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;

public class SubscriptionDescriptorRegistryTest {

    @Test
    public void shouldMaintainARegistryOfServiceNamesToSubscriptionDescriptions() throws Exception {

        final String event_listener = "EVENT_LISTENER";
        final String event_processor = "EVENT_PROCESSOR";
        final SubscriptionDescriptor subscriptionDescriptor_1 = subscriptionDescriptor()
                .withServiceComponent(event_listener)
                .build();

        final SubscriptionDescriptor subscriptionDescriptor_2 = subscriptionDescriptor()
                .withServiceComponent(event_processor)
                .build();

        final Stream<SubscriptionDescriptor> subscriptionDescriptors = Stream.of(
                subscriptionDescriptor_1,
                subscriptionDescriptor_2
        );

        final SubscriptionDescriptorRegistry subscriptionDescriptorRegistry = new SubscriptionDescriptorRegistry(subscriptionDescriptors);

        assertThat(subscriptionDescriptorRegistry.getSubscriptionDescriptorFor(event_listener), is(Optional.of(subscriptionDescriptor_1)));
        assertThat(subscriptionDescriptorRegistry.getSubscriptionDescriptorFor(event_processor), is(Optional.of(subscriptionDescriptor_2)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnEmptyIfNoSubscriptionIsFoundForASpecifiedServiceName() throws Exception {

        final SubscriptionDescriptorRegistry subscriptionDescriptorRegistry = new SubscriptionDescriptorRegistry(Stream.empty());

        assertThat(subscriptionDescriptorRegistry.getSubscriptionDescriptorFor("some-non-existent-service"), is(empty()));
    }

    @Test
    public void shouldGetASubscriptionByNameBySearchingAllASubscriptionDescriptors() throws Exception {

        final Subscription subscription_1_1 = subscription().withName("subscription_1_1").build();
        final Subscription subscription_1_2 = subscription().withName("subscription_1_2").build();
        final Subscription subscription_2_1 = subscription().withName("subscription_2_1").build();
        final Subscription subscription_2_2 = subscription().withName("subscription_2_2").build();


        final String event_listener = "EVENT_LISTENER";
        final String event_processor = "EVENT_PROCESSOR";

        final SubscriptionDescriptor subscriptionDescriptor_1 = subscriptionDescriptor()
                .withServiceComponent(event_listener)
                .withSubscriptions(asList(
                        subscription_1_1,
                        subscription_1_2
                ))
                .build();

        final SubscriptionDescriptor subscriptionDescriptor_2 = subscriptionDescriptor()
                .withServiceComponent(event_processor)
                .withSubscriptions(asList(
                        subscription_2_1,
                        subscription_2_2
                ))
                .build();

        final Stream<SubscriptionDescriptor> subscriptionDescriptors = Stream.of(
                subscriptionDescriptor_1,
                subscriptionDescriptor_2
        );

        final SubscriptionDescriptorRegistry subscriptionDescriptorRegistry = new SubscriptionDescriptorRegistry(subscriptionDescriptors);

        assertThat(subscriptionDescriptorRegistry.getSubscription("subscription_2_1"), is(subscription_2_1));
    }

    @Test
    public void shouldThrowARegistryExceptionIfNoSubscriptionCanBeFoundWithTheSpecifiedSubscriptionName() throws Exception {

        final String thisSubscriptionDoesNotExist = "thisSubscriptionDoesNotExist";

        final Subscription subscription_1_1 = subscription().withName("subscription_1_1").build();
        final Subscription subscription_1_2 = subscription().withName("subscription_1_2").build();
        final Subscription subscription_2_1 = subscription().withName("subscription_2_1").build();
        final Subscription subscription_2_2 = subscription().withName("subscription_2_2").build();

        final SubscriptionDescriptor subscriptionDescriptor_1 = subscriptionDescriptor()
                .withServiceComponent("EVENT_LISTENER")
                .withSubscriptions(asList(
                        subscription_1_1,
                        subscription_1_2
                ))
                .build();

        final SubscriptionDescriptor subscriptionDescriptor_2 = subscriptionDescriptor()
                .withServiceComponent("EVENT_PROCESSOR")
                .withSubscriptions(asList(
                        subscription_2_1,
                        subscription_2_2
                ))
                .build();

        final Stream<SubscriptionDescriptor> subscriptionDescriptors = Stream.of(
                subscriptionDescriptor_1,
                subscriptionDescriptor_2
        );

        final SubscriptionDescriptorRegistry subscriptionDescriptorRegistry = new SubscriptionDescriptorRegistry(subscriptionDescriptors);

        try {
            subscriptionDescriptorRegistry.getSubscription(thisSubscriptionDoesNotExist);
            fail();
        } catch (final RegistryException expected) {
            assertThat(expected.getMessage(), is("Failed to find subscription 'thisSubscriptionDoesNotExist' in registry"));
        }
    }

    @Test
    public void shouldThrowARegistryExceptionIfDuplicateSubscriptionDescriptorFoundForServiceComponentName() throws Exception {

        final SubscriptionDescriptor subscriptionDescriptor_1 = subscriptionDescriptor()
                .withServiceComponent("EVENT_LISTENER")
                .build();

        final SubscriptionDescriptor subscriptionDescriptor_2 = subscriptionDescriptor()
                .withServiceComponent("EVENT_LISTENER")
                .build();

        final Stream<SubscriptionDescriptor> subscriptionDescriptors = Stream.of(
                subscriptionDescriptor_1,
                subscriptionDescriptor_2
        );
        try {
           new SubscriptionDescriptorRegistry(subscriptionDescriptors);
            fail();
        } catch (final RegistryException expected) {
            assertThat(expected.getMessage(), is("Duplicate subscription descriptor for service component: EVENT_LISTENER"));
        }
    }
}
