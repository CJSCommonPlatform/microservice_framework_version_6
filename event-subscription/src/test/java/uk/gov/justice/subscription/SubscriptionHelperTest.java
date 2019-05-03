package uk.gov.justice.subscription;


import static org.hamcrest.Matchers.is;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.yaml.YamlParserException;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Event;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionHelperTest {

    @Test
    public void shouldSortBySubscriptionPrioritisation() {

        final List<Event> eventList = asList(mock(Event.class),mock(Event.class));

        final Subscription subscription1 = new Subscription("name1",eventList,"eventSourceName1", "1");
        final Subscription subscription2 = new Subscription("name2",eventList,"eventSourceName3", "2");
        final Subscription subscription3 = new Subscription("name3",eventList,"eventSourceName1", "3");

        final List<Subscription> subscriptions = asList(subscription2,subscription3,subscription1);
        final SubscriptionsDescriptor subscriptionsDescriptor = new SubscriptionsDescriptor("specVersion", "service", "serviceComponent", subscriptions);

        final SubscriptionHelper subscriptionHelper = new SubscriptionHelper();

        subscriptionHelper.sortSubscriptionsByPrioritisation(subscriptionsDescriptor);

        assertThat(subscriptionsDescriptor.getSubscriptions().get(0), is(subscription1));
        assertThat(subscriptionsDescriptor.getSubscriptions().get(1), is(subscription2));
        assertThat(subscriptionsDescriptor.getSubscriptions().get(2), is(subscription3));

    }

    @Test
    public void shouldSortBySubscriptionPrioritisationNullsFirst() {

        final List<Event> eventList = asList(mock(Event.class),mock(Event.class));

        final Subscription subscription1 = new Subscription("name1",eventList,"eventSourceName1", "1");
        final Subscription subscription2 = new Subscription("name2",eventList,"eventSourceName3", null);
        final Subscription subscription3 = new Subscription("name3",eventList,"eventSourceName1", "3");

        final List<Subscription> subscriptions = asList(subscription2,subscription3,subscription1);

        final SubscriptionsDescriptor subscriptionsDescriptor = new SubscriptionsDescriptor("specVersion", "service", "serviceComponent", subscriptions);

        final SubscriptionHelper subscriptionHelper = new SubscriptionHelper();

        subscriptionHelper.sortSubscriptionsByPrioritisation(subscriptionsDescriptor);

        assertThat(subscriptionsDescriptor.getSubscriptions().get(0), is(subscription2));
        assertThat(subscriptionsDescriptor.getSubscriptions().get(1), is(subscription1));
        assertThat(subscriptionsDescriptor.getSubscriptions().get(2), is(subscription3));

    }

    @Test
    public void shouldThrowExceptionIfPrioritisationNotNumber() {

        final List<Event> eventList = asList(mock(Event.class),mock(Event.class));

        final Subscription subscription1 = new Subscription("name1",eventList,"eventSourceName1", "1");
        final Subscription subscription2 = new Subscription("name2",eventList,"eventSourceName3", "wrongPrioritisation");
        final Subscription subscription3 = new Subscription("name3",eventList,"eventSourceName1", "3");

        final List<Subscription> subscriptions = asList(subscription2,subscription3,subscription1);

        final SubscriptionsDescriptor subscriptionsDescriptor = new SubscriptionsDescriptor("specVersion", "service", "serviceComponent", subscriptions);

        final SubscriptionHelper subscriptionHelper = new SubscriptionHelper();

        try {
            subscriptionHelper.sortSubscriptionsByPrioritisation(subscriptionsDescriptor);
            fail();
        }catch (final YamlParserException expected){
            assertThat(expected.getMessage(), is("Incorrect prioritisation number: wrongPrioritisation in subscription-descriptor.yaml"));
        }
    }
}