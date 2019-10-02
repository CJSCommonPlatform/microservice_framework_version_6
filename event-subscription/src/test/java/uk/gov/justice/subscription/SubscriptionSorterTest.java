package uk.gov.justice.subscription;


import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import uk.gov.justice.subscription.domain.subscriptiondescriptor.Event;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.SubscriptionsDescriptor;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionSorterTest {

    @Test
    public void shouldSortBySubscriptionPrioritisation() {

        final List<Event> eventList = asList(mock(Event.class), mock(Event.class));

        final Subscription subscription1 = new Subscription("name1", eventList, "eventSourceName1", 1);
        final Subscription subscription2 = new Subscription("name2", eventList, "eventSourceName3", 2);
        final Subscription subscription3 = new Subscription("name3", eventList, "eventSourceName1", 3);

        final List<Subscription> subscriptions = asList(subscription2, subscription3, subscription1);
        final SubscriptionsDescriptor subscriptionsDescriptor = new SubscriptionsDescriptor("specVersion", "service", "serviceComponent", 1, subscriptions);

        final SubscriptionSorter subscriptionSorter = new SubscriptionSorter();

        subscriptionSorter.sortSubscriptionsByPrioritisation(subscriptionsDescriptor);

        assertThat(subscriptionsDescriptor.getSubscriptions().get(0), is(subscription1));
        assertThat(subscriptionsDescriptor.getSubscriptions().get(1), is(subscription2));
        assertThat(subscriptionsDescriptor.getSubscriptions().get(2), is(subscription3));

    }

    @Test
    public void shouldSortBySubscriptionPrioritisationNullsFirst() {

        final List<Event> eventList = asList(mock(Event.class), mock(Event.class));

        final Subscription subscription1 = new Subscription("name1", eventList, "eventSourceName1", 1);
        final Subscription subscription2 = new Subscription("name2", eventList, "eventSourceName3", 0);
        final Subscription subscription3 = new Subscription("name3", eventList, "eventSourceName1", 3);

        final List<Subscription> subscriptions = asList(subscription2, subscription3, subscription1);

        final SubscriptionsDescriptor subscriptionsDescriptor = new SubscriptionsDescriptor("specVersion", "service", "serviceComponent", 1, subscriptions);

        final SubscriptionSorter subscriptionSorter = new SubscriptionSorter();

        subscriptionSorter.sortSubscriptionsByPrioritisation(subscriptionsDescriptor);

        assertThat(subscriptionsDescriptor.getSubscriptions().get(0), is(subscription2));
        assertThat(subscriptionsDescriptor.getSubscriptions().get(1), is(subscription1));
        assertThat(subscriptionsDescriptor.getSubscriptions().get(2), is(subscription3));

    }
}
