package uk.gov.justice.raml.jms.converters;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.DELETE;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.HEAD;
import static org.raml.model.ActionType.OPTIONS;
import static org.raml.model.ActionType.PATCH;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.TRACE;

import uk.gov.justice.subscription.domain.Subscription;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.Action;
import org.raml.model.Resource;


@RunWith(MockitoJUnitRunner.class)
public class ResourcesListToSubscriptionListConverterTest {

    @Mock
    private RamlResourceToSubscriptionConverter ramlResourceToSubscriptionConverter;

    @InjectMocks
    private ResourcesListToSubscriptionListConverter resourcesListToSubscriptionListConverter;

    @Test
    public void shouldConvertAListOfResourcesToAListOfSubscriptions() throws Exception {

        final Resource resource_1 = mock(Resource.class);
        final Resource resource_2 = mock(Resource.class);
        final Resource resource_3 = mock(Resource.class);

        final Action action_1 = mock(Action.class);
        final Action action_2 = mock(Action.class);
        final Action action_3 = mock(Action.class);

        final Subscription subscription_1 = mock(Subscription.class);
        final Subscription subscription_2 = mock(Subscription.class);
        final Subscription subscription_3 = mock(Subscription.class);

        when(resource_1.getAction(POST)).thenReturn(action_1);
        when(resource_2.getAction(POST)).thenReturn(action_2);
        when(resource_3.getAction(POST)).thenReturn(action_3);

        when(ramlResourceToSubscriptionConverter.asSubscription(resource_1)).thenReturn(subscription_1);
        when(ramlResourceToSubscriptionConverter.asSubscription(resource_2)).thenReturn(subscription_2);
        when(ramlResourceToSubscriptionConverter.asSubscription(resource_3)).thenReturn(subscription_3);

        final Collection<Resource> resources = asList(resource_1, resource_2, resource_3);

        final List<Subscription> subscriptions = resourcesListToSubscriptionListConverter.getSubscriptions(resources);

        assertThat(subscriptions.size(), is(3));
        assertThat(subscriptions, hasItem(subscription_1));
        assertThat(subscriptions, hasItem(subscription_2));
        assertThat(subscriptions, hasItem(subscription_3));
    }

    @Test
    public void shouldOnlyConvertResourcesThatHaveAnActionOfPost() throws Exception {

        final Resource resource_1 = mock(Resource.class);
        final Resource resource_2 = mock(Resource.class);
        final Resource resource_3 = mock(Resource.class);
        final Resource resource_4 = mock(Resource.class);
        final Resource resource_5 = mock(Resource.class);
        final Resource resource_6 = mock(Resource.class);
        final Resource resource_7 = mock(Resource.class);

        final Action action_1 = mock(Action.class);

        final Subscription subscription_1 = mock(Subscription.class);

        when(resource_1.getAction(POST)).thenReturn(action_1);
        when(resource_2.getAction(GET)).thenReturn(null);
        when(resource_3.getAction(DELETE)).thenReturn(null);
        when(resource_4.getAction(HEAD)).thenReturn(null);
        when(resource_5.getAction(OPTIONS)).thenReturn(null);
        when(resource_6.getAction(PATCH)).thenReturn(null);
        when(resource_7.getAction(TRACE)).thenReturn(null);

        when(ramlResourceToSubscriptionConverter.asSubscription(resource_1)).thenReturn(subscription_1);

        final Collection<Resource> resources = asList(
                resource_1,
                resource_2,
                resource_3,
                resource_4,
                resource_5,
                resource_6,
                resource_7
        );

        final List<Subscription> subscriptions = resourcesListToSubscriptionListConverter.getSubscriptions(resources);

        assertThat(subscriptions.size(), is(1));

        assertThat(subscriptions.get(0), is(subscription_1));
    }

    @Test
    public void shouldReturnAnEmptyListOfSubscriptionsIfNoneOfTheActionsArePost() throws Exception {

        final Resource resource_1 = mock(Resource.class);
        final Resource resource_2 = mock(Resource.class);
        final Resource resource_3 = mock(Resource.class);
        final Resource resource_4 = mock(Resource.class);
        final Resource resource_5 = mock(Resource.class);
        final Resource resource_6 = mock(Resource.class);

        when(resource_1.getAction(GET)).thenReturn(null);
        when(resource_2.getAction(DELETE)).thenReturn(null);
        when(resource_3.getAction(HEAD)).thenReturn(null);
        when(resource_4.getAction(OPTIONS)).thenReturn(null);
        when(resource_5.getAction(PATCH)).thenReturn(null);
        when(resource_6.getAction(TRACE)).thenReturn(null);


        final Collection<Resource> resources = asList(
                resource_1,
                resource_2,
                resource_3,
                resource_4,
                resource_5,
                resource_6
        );

        final List<Subscription> subscriptions = resourcesListToSubscriptionListConverter.getSubscriptions(resources);

        assertThat(subscriptions.size(), is(0));

        verifyZeroInteractions(ramlResourceToSubscriptionConverter);
    }
}
