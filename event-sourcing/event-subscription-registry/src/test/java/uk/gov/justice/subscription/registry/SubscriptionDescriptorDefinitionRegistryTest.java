package uk.gov.justice.subscription.registry;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.subscription.domain.builders.SubscriptionBuilder.subscription;
import static uk.gov.justice.subscription.domain.builders.SubscriptionDescriptorDefinitionBuilder.subscriptionDescriptorDefinition;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionDescriptorDefinition;

import java.util.Set;

import com.google.common.collect.Sets;
import org.junit.Test;

public class SubscriptionDescriptorDefinitionRegistryTest {


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

        final Set<SubscriptionDescriptorDefinition> subscriptionDescriptors = Sets.newHashSet(
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

        final Set<SubscriptionDescriptorDefinition> subscriptionDescriptorDefinitions = Sets.newHashSet(
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
    public void shouldNotAllowDuplicateSubscriptionDescriptorDefinition() throws Exception {

        final SubscriptionDescriptorDefinition subscriptionDescriptorDefinition_1 = subscriptionDescriptorDefinition()
                .withServiceComponent("EVENT_LISTENER")
                .build();

        final SubscriptionDescriptorDefinition subscriptionDescriptorDefinition_2 = subscriptionDescriptorDefinition()
                .withServiceComponent("EVENT_LISTENER")
                .build();

        final Set<SubscriptionDescriptorDefinition> subscriptionDescriptorDefinitions = Sets.newHashSet(
                subscriptionDescriptorDefinition_1,
                subscriptionDescriptorDefinition_2
        );

        final SubscriptionDescriptorDefinitionRegistry subscriptionDescriptorDefinitionRegistry = new SubscriptionDescriptorDefinitionRegistry(subscriptionDescriptorDefinitions);

        assertThat(subscriptionDescriptorDefinitionRegistry.subscriptionDescriptorDefinitions().size(), is(1));

    }

    @Test
    public void shouldThrowARegistryExceptionIfNoServiceComponentNameFoundWithTheSpecifiedSubscriptionName() throws Exception {

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

        final Set<SubscriptionDescriptorDefinition> subscriptionDescriptorDefinitions = Sets.newHashSet(
                subscriptionDescriptorDefinition_1,
                subscriptionDescriptorDefinition_2
        );

        final SubscriptionDescriptorDefinitionRegistry subscriptionDescriptorRegistry = new SubscriptionDescriptorDefinitionRegistry(subscriptionDescriptorDefinitions);

        try {
            subscriptionDescriptorRegistry.findComponentNameBy(thisSubscriptionDoesNotExist);
            fail();
        } catch (final RegistryException expected) {
            assertThat(expected.getMessage(), is("Failed to find service component name in registry for subscription 'thisSubscriptionDoesNotExist' "));
        }
    }

    @Test
    public void shouldFindServiceComponentNameWithTheSpecifiedSubscriptionName() throws Exception {
        final String event_listener = "EVENT_LISTENER";

        final String subscriptionName = "subscription_1_1";
        final Subscription subscription_1_1 = subscription().withName(subscriptionName).build();
        final Subscription subscription_1_2 = subscription().withName("subscription_1_2").build();
        final Subscription subscription_2_1 = subscription().withName("subscription_2_1").build();
        final Subscription subscription_2_2 = subscription().withName("subscription_2_2").build();


        final SubscriptionDescriptorDefinition subscriptionDescriptorDefinition_1 = subscriptionDescriptorDefinition()
                .withServiceComponent(event_listener)
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

        final Set<SubscriptionDescriptorDefinition> subscriptionDescriptorDefinitions = Sets.newHashSet(
                subscriptionDescriptorDefinition_1,
                subscriptionDescriptorDefinition_2
        );

        final SubscriptionDescriptorDefinitionRegistry subscriptionDescriptorRegistry = new SubscriptionDescriptorDefinitionRegistry(subscriptionDescriptorDefinitions);
        final String componentName = subscriptionDescriptorRegistry.findComponentNameBy(subscriptionName);

        assertThat(componentName , is(event_listener));
    }
}