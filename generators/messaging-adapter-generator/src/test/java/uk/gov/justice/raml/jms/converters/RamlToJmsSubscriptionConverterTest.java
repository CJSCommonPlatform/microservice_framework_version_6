package uk.gov.justice.raml.jms.converters;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.domain.subscriptiondescriptor.Subscription;
import uk.gov.justice.domain.subscriptiondescriptor.SubscriptionDescriptor;
import uk.gov.justice.domain.subscriptiondescriptor.SubscriptionDescriptorDef;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.Raml;
import org.raml.model.Resource;

@RunWith(MockitoJUnitRunner.class)
public class RamlToJmsSubscriptionConverterTest {

    @Mock
    private SubscriptionNamesGenerator subscriptionNamesGenerator;

    @Mock
    private ResourcesListToSubscriptionListConverter resourcesListToSubscriptionListConverter;

    @InjectMocks
    private RamlToJmsSubscriptionConverter ramlToJmsSubscriptionConverter;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCreateASchemaObjectFromARamlObject() throws Exception {

        final String componentName = "EVENT_LISTENER";
        final String contextName = "people";

        final String baseUri = "message://command/handler/message/people";

        final Raml raml = mock(Raml.class, RETURNS_DEEP_STUBS.get());

        final Resource resource_1 = mock(Resource.class);
        final Resource resource_2 = mock(Resource.class);
        final Resource resource_3 = mock(Resource.class);

        final Subscription subscription_1 = mock(Subscription.class);
        final Subscription subscription_2 = mock(Subscription.class);
        final Subscription subscription_3 = mock(Subscription.class);

        final List<Subscription> subscriptions = asList(subscription_1, subscription_2, subscription_3);

        when(raml.getBaseUri()).thenReturn(baseUri);
        when(subscriptionNamesGenerator.createContextNameFrom(baseUri)).thenReturn(contextName);
        when(raml.getResources().values()).thenReturn(asList(resource_1, resource_2, resource_3));
        when(resourcesListToSubscriptionListConverter.getSubscriptions(raml.getResources().values())).thenReturn(subscriptions);


        final SubscriptionDescriptorDef subscriptionDescriptorDef = ramlToJmsSubscriptionConverter.convert(
                raml,
                componentName
        );

        final SubscriptionDescriptor subscriptionDescriptor = subscriptionDescriptorDef.getSubscriptionDescriptor();

        assertThat(subscriptionDescriptor.getSpecVersion(), is("1.0.0"));
        assertThat(subscriptionDescriptor.getService(), is(contextName));
        assertThat(subscriptionDescriptor.getServiceComponent(), is(componentName));

        assertThat(subscriptionDescriptor.getSubscriptions().size(), is(3));

        assertThat(subscriptionDescriptor.getSubscriptions(), hasItem(subscription_1));
        assertThat(subscriptionDescriptor.getSubscriptions(), hasItem(subscription_2));
        assertThat(subscriptionDescriptor.getSubscriptions(), hasItem(subscription_3));
    }
}
