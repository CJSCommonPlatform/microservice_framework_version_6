package uk.gov.justice.subscription.registry;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.subscription.domain.builders.SubscriptionBuilder.subscription;
import static uk.gov.justice.subscription.domain.builders.SubscriptionsDescriptorBuilder.subscriptionsDescriptor;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.util.Set;

import com.google.common.collect.Sets;
import org.junit.Test;

public class SubscriptionsDescriptorRegistryTest {


    @Test
    public void shouldGetASubscriptionByNameBySearchingAllASubscriptionDescriptorDefinitions() {

        final Subscription subscription_1_1 = subscription().withName("subscription_1_1").build();
        final Subscription subscription_1_2 = subscription().withName("subscription_1_2").build();
        final Subscription subscription_2_1 = subscription().withName("subscription_2_1").build();
        final Subscription subscription_2_2 = subscription().withName("subscription_2_2").build();


        final String event_listener = "EVENT_LISTENER";
        final String event_processor = "EVENT_PROCESSOR";

        final SubscriptionsDescriptor subscriptionsDescriptor_1 = subscriptionsDescriptor()
                .withServiceComponent(event_listener)
                .withSubscriptions(asList(
                        subscription_1_1,
                        subscription_1_2
                ))
                .build();

        final SubscriptionsDescriptor subscriptionsDescriptor_2 = subscriptionsDescriptor()
                .withServiceComponent(event_processor)
                .withSubscriptions(asList(
                        subscription_2_1,
                        subscription_2_2
                ))
                .build();

        final Set<SubscriptionsDescriptor> subscriptionDescriptors = Sets.newHashSet(
                subscriptionsDescriptor_1,
                subscriptionsDescriptor_2
        );

        final SubscriptionsDescriptorsRegistry subscriptionDescriptorRegistry = new SubscriptionsDescriptorsRegistry(subscriptionDescriptors);

        assertThat(subscriptionDescriptorRegistry.getSubscriptionFor("subscription_2_1"), is(subscription_2_1));
    }

    @Test
    public void shouldThrowARegistryExceptionIfNoSubscriptionCanBeFoundWithTheSpecifiedSubscriptionName() {

        final String thisSubscriptionDoesNotExist = "thisSubscriptionDoesNotExist";

        final Subscription subscription_1_1 = subscription().withName("subscription_1_1").build();
        final Subscription subscription_1_2 = subscription().withName("subscription_1_2").build();
        final Subscription subscription_2_1 = subscription().withName("subscription_2_1").build();
        final Subscription subscription_2_2 = subscription().withName("subscription_2_2").build();

        final SubscriptionsDescriptor subscriptionsDescriptor_1 = subscriptionsDescriptor()
                .withServiceComponent("EVENT_LISTENER")
                .withSubscriptions(asList(
                        subscription_1_1,
                        subscription_1_2
                ))
                .build();

        final SubscriptionsDescriptor subscriptionsDescriptor_2 = subscriptionsDescriptor()
                .withServiceComponent("EVENT_PROCESSOR")
                .withSubscriptions(asList(
                        subscription_2_1,
                        subscription_2_2
                ))
                .build();

        final Set<SubscriptionsDescriptor> subscriptionsDescriptors = Sets.newHashSet(
                subscriptionsDescriptor_1,
                subscriptionsDescriptor_2
        );

        final SubscriptionsDescriptorsRegistry subscriptionDescriptorRegistry = new SubscriptionsDescriptorsRegistry(subscriptionsDescriptors);

        try {
            subscriptionDescriptorRegistry.getSubscriptionFor(thisSubscriptionDoesNotExist);
            fail();
        } catch (final RegistryException expected) {
            assertThat(expected.getMessage(), is("Failed to find subscription 'thisSubscriptionDoesNotExist' in registry"));
        }
    }

    @Test
    public void shouldNotAllowDuplicateSubscriptionDescriptorDefinition() {

        final SubscriptionsDescriptor subscriptionsDescriptor_1 = subscriptionsDescriptor()
                .withServiceComponent("EVENT_LISTENER")
                .build();

        final SubscriptionsDescriptor subscriptionsDescriptor_2 = subscriptionsDescriptor()
                .withServiceComponent("EVENT_LISTENER")
                .build();

        final Set<SubscriptionsDescriptor> subscriptionsDescriptors = Sets.newHashSet(
                subscriptionsDescriptor_1,
                subscriptionsDescriptor_2
        );

        final SubscriptionsDescriptorsRegistry subscriptionsDescriptorsRegistry = new SubscriptionsDescriptorsRegistry(subscriptionsDescriptors);

        assertThat(subscriptionsDescriptorsRegistry.subscriptionsDescriptors().size(), is(1));

    }

    @Test
    public void shouldThrowARegistryExceptionIfNoServiceComponentNameFoundWithTheSpecifiedSubscriptionName() {

        final String thisSubscriptionDoesNotExist = "thisSubscriptionDoesNotExist";

        final Subscription subscription_1_1 = subscription().withName("subscription_1_1").build();
        final Subscription subscription_1_2 = subscription().withName("subscription_1_2").build();
        final Subscription subscription_2_1 = subscription().withName("subscription_2_1").build();
        final Subscription subscription_2_2 = subscription().withName("subscription_2_2").build();

        final SubscriptionsDescriptor subscriptionsDescriptor_1 = subscriptionsDescriptor()
                .withServiceComponent("EVENT_LISTENER")
                .withSubscriptions(asList(
                        subscription_1_1,
                        subscription_1_2
                ))
                .build();

        final SubscriptionsDescriptor subscriptionsDescriptor_2 = subscriptionsDescriptor()
                .withServiceComponent("EVENT_PROCESSOR")
                .withSubscriptions(asList(
                        subscription_2_1,
                        subscription_2_2
                ))
                .build();

        final Set<SubscriptionsDescriptor> subscriptionsDescriptors = Sets.newHashSet(
                subscriptionsDescriptor_1,
                subscriptionsDescriptor_2
        );

        final SubscriptionsDescriptorsRegistry subscriptionDescriptorRegistry = new SubscriptionsDescriptorsRegistry(subscriptionsDescriptors);

        try {
            subscriptionDescriptorRegistry.findComponentNameBy(thisSubscriptionDoesNotExist);
            fail();
        } catch (final RegistryException expected) {
            assertThat(expected.getMessage(), is("Failed to find service component name in registry for subscription 'thisSubscriptionDoesNotExist' "));
        }
    }

    @Test
    public void shouldFindServiceComponentNameWithTheSpecifiedSubscriptionName() {
        final String event_listener = "EVENT_LISTENER";

        final String subscriptionName = "subscription_1_1";
        final Subscription subscription_1_1 = subscription().withName(subscriptionName).build();
        final Subscription subscription_1_2 = subscription().withName("subscription_1_2").build();
        final Subscription subscription_2_1 = subscription().withName("subscription_2_1").build();
        final Subscription subscription_2_2 = subscription().withName("subscription_2_2").build();


        final SubscriptionsDescriptor subscriptionsDescriptor_1 = subscriptionsDescriptor()
                .withServiceComponent(event_listener)
                .withSubscriptions(asList(
                        subscription_1_1,
                        subscription_1_2
                ))
                .build();

        final SubscriptionsDescriptor subscriptionsDescriptor_2 = subscriptionsDescriptor()
                .withServiceComponent("EVENT_PROCESSOR")
                .withSubscriptions(asList(
                        subscription_2_1,
                        subscription_2_2
                ))
                .build();

        final Set<SubscriptionsDescriptor> subscriptionsDescriptors = Sets.newHashSet(
                subscriptionsDescriptor_1,
                subscriptionsDescriptor_2
        );

        final SubscriptionsDescriptorsRegistry subscriptionDescriptorRegistry = new SubscriptionsDescriptorsRegistry(subscriptionsDescriptors);
        final String componentName = subscriptionDescriptorRegistry.findComponentNameBy(subscriptionName);

        assertThat(componentName , is(event_listener));
    }
}