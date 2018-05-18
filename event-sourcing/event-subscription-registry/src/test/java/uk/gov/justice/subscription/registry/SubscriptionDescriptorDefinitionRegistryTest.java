package uk.gov.justice.subscription.registry;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.subscription.domain.builders.SubscriptionBuilder.subscription;
import static uk.gov.justice.subscription.domain.builders.SubscriptionDescriptorDefinitionBuilder.subscriptionDescriptorDefinition;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDefinition;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;

public class SubscriptionDescriptorDefinitionRegistryTest {

    @Test
    public void shouldMaintainARegistryOfServiceComponentNamesToSubscriptionDescriptionDefinitions() throws Exception {

        final String event_listener = "EVENT_LISTENER";
        final String event_processor = "EVENT_PROCESSOR";
        final SubscriptionDescriptorDefinition subscriptionDescriptor_1 = subscriptionDescriptorDefinition()
                .withServiceComponent(event_listener)
                .build();

        final SubscriptionDescriptorDefinition subscriptionDescriptor_2 = subscriptionDescriptorDefinition()
                .withServiceComponent(event_processor)
                .build();

        final Stream<SubscriptionDescriptorDefinition> subscriptionDescriptorDefinitions = Stream.of(
                subscriptionDescriptor_1,
                subscriptionDescriptor_2
        );

        final SubscriptionDescriptorDefinitionRegistry subscriptionDescriptorRegistry = new SubscriptionDescriptorDefinitionRegistry(subscriptionDescriptorDefinitions);

        assertThat(subscriptionDescriptorRegistry.getSubscriptionDescriptorDescriptorFor(event_listener), is(Optional.of(subscriptionDescriptor_1)));
        assertThat(subscriptionDescriptorRegistry.getSubscriptionDescriptorDescriptorFor(event_processor), is(Optional.of(subscriptionDescriptor_2)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnEmptyIfNoSubscriptionIsFoundForASpecifiedServiceName() throws Exception {

        final SubscriptionDescriptorDefinitionRegistry subscriptionDescriptorRegistry = new SubscriptionDescriptorDefinitionRegistry(Stream.empty());

        assertThat(subscriptionDescriptorRegistry.getSubscriptionDescriptorDescriptorFor("some-non-existent-service"), is(empty()));
    }

    @Test
    public void shouldGetASubscriptionByNameBySearchingAllASubscriptionDescriptorDefinitions() throws Exception {

        final Subscription subscription_1_1 = subscription().withName("subscription_1_1").build();
        final Subscription subscription_1_2 = subscription().withName("subscription_1_2").build();
        final Subscription subscription_2_1 = subscription().withName("subscription_2_1").build();
        final Subscription subscription_2_2 = subscription().withName("subscription_2_2").build();


        final String event_listener = "EVENT_LISTENER";
        final String event_processor = "EVENT_PROCESSOR";

        final SubscriptionDescriptorDefinition subscriptionDescriptorDefinition_1 = subscriptionDescriptorDefinition()
                .withServiceComponent(event_listener)
                .withSubscriptions(asList(
                        subscription_1_1,
                        subscription_1_2
                ))
                .build();

        final SubscriptionDescriptorDefinition subscriptionDescriptorDefinition_2 = subscriptionDescriptorDefinition()
                .withServiceComponent(event_processor)
                .withSubscriptions(asList(
                        subscription_2_1,
                        subscription_2_2
                ))
                .build();

        final Stream<SubscriptionDescriptorDefinition> subscriptionDescriptors = Stream.of(
                subscriptionDescriptorDefinition_1,
                subscriptionDescriptorDefinition_2
        );

        final SubscriptionDescriptorDefinitionRegistry subscriptionDescriptorRegistry = new SubscriptionDescriptorDefinitionRegistry(subscriptionDescriptors);

        assertThat(subscriptionDescriptorRegistry.getSubscriptionFor("subscription_2_1"), is(subscription_2_1));
    }

    @Test
    public void shouldThrowARegistryExceptionIfNoSubscriptionCanBeFoundWithTheSpecifiedSubscriptionName() throws Exception {

        final String thisSubscriptionDoesNotExist = "thisSubscriptionDoesNotExist";

        final Subscription subscription_1_1 = subscription().withName("subscription_1_1").build();
        final Subscription subscription_1_2 = subscription().withName("subscription_1_2").build();
        final Subscription subscription_2_1 = subscription().withName("subscription_2_1").build();
        final Subscription subscription_2_2 = subscription().withName("subscription_2_2").build();

        final SubscriptionDescriptorDefinition subscriptionDescriptorDefinition_1 = subscriptionDescriptorDefinition()
                .withServiceComponent("EVENT_LISTENER")
                .withSubscriptions(asList(
                        subscription_1_1,
                        subscription_1_2
                ))
                .build();

        final SubscriptionDescriptorDefinition subscriptionDescriptorDefinition_2 = subscriptionDescriptorDefinition()
                .withServiceComponent("EVENT_PROCESSOR")
                .withSubscriptions(asList(
                        subscription_2_1,
                        subscription_2_2
                ))
                .build();

        final Stream<SubscriptionDescriptorDefinition> subscriptionDescriptorDefinitions = Stream.of(
                subscriptionDescriptorDefinition_1,
                subscriptionDescriptorDefinition_2
        );

        final SubscriptionDescriptorDefinitionRegistry subscriptionDescriptorRegistry = new SubscriptionDescriptorDefinitionRegistry(subscriptionDescriptorDefinitions);

        try {
            subscriptionDescriptorRegistry.getSubscriptionFor(thisSubscriptionDoesNotExist);
            fail();
        } catch (final RegistryException expected) {
            assertThat(expected.getMessage(), is("Failed to find subscription 'thisSubscriptionDoesNotExist' in registry"));
        }
    }

    @Test
    public void shouldThrowARegistryExceptionIfDuplicateSubscriptionDescriptorDefinitionFoundForServiceComponentName() throws Exception {

        final SubscriptionDescriptorDefinition subscriptionDescriptorDefinition_1 = subscriptionDescriptorDefinition()
                .withServiceComponent("EVENT_LISTENER")
                .build();

        final SubscriptionDescriptorDefinition subscriptionDescriptorDefinition_2 = subscriptionDescriptorDefinition()
                .withServiceComponent("EVENT_LISTENER")
                .build();

        final Stream<SubscriptionDescriptorDefinition> subscriptionDescriptorDefinitions = Stream.of(
                subscriptionDescriptorDefinition_1,
                subscriptionDescriptorDefinition_2
        );
        try {
            new SubscriptionDescriptorDefinitionRegistry(subscriptionDescriptorDefinitions);
            fail();
        } catch (final RegistryException expected) {
            assertThat(expected.getMessage(), is("Duplicate subscription descriptor for service component: EVENT_LISTENER"));
        }
    }
}
