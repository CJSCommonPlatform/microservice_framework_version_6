package uk.gov.justice.raml.jms.converters;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.POST;

import uk.gov.justice.subscription.domain.Event;
import uk.gov.justice.subscription.domain.Eventsource;
import uk.gov.justice.subscription.domain.Location;
import uk.gov.justice.subscription.domain.Subscription;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.raml.model.MimeType;
import org.raml.model.Resource;


@RunWith(MockitoJUnitRunner.class)
public class RamlResourceToSubscriptionConverterTest {

    @Mock
    private SubscriptionNamesGenerator subscriptionNamesGenerator;

    @Mock
    private RamlMimeTypeListToEventListConverter ramlMimeTypeListToEventListConverter;

    @Mock
    private JmsUriGenerator jmsUriGenerator;

    @InjectMocks
    private RamlResourceToSubscriptionConverter ramlResourceToSubscriptionConverter;

    @Test
    public void shouldConvertARamlResourceToASubscription() throws Exception {

        final String resourceUri = "resourceUri";
        final String subscriptionName = "subscriptionName";

        final String eventSourceName = "eventSourceName";
        final String jmsUri = "jmsUri";

        final Resource resource = mock(Resource.class, RETURNS_DEEP_STUBS.get());

        final Event event_1 = mock(Event.class);
        final Event event_2 = mock(Event.class);

        final Collection<MimeType> mimeTypes = asList(mock(MimeType.class), mock(MimeType.class));

        when(resource.getUri()).thenReturn(resourceUri);
        when(subscriptionNamesGenerator.createSubscriptionNameFrom(resourceUri)).thenReturn(subscriptionName);

        when(resource.getAction(POST).getBody().values()).thenReturn(mimeTypes);
        when(ramlMimeTypeListToEventListConverter.toEvents(mimeTypes)).thenReturn(asList(event_1, event_2));
        when(subscriptionNamesGenerator.createEventSourceNameFrom(resourceUri)).thenReturn(eventSourceName);
        when(jmsUriGenerator.createJmsUriFrom(resourceUri)).thenReturn(jmsUri);

        final Subscription subscription = ramlResourceToSubscriptionConverter.asSubscription(resource);

        assertThat(subscription.getName(), is(subscriptionName));

        final List<Event> events = subscription.getEvents();

        assertThat(events.size(), is(2));
        assertThat(events.get(0), is(event_1));
        assertThat(events.get(1), is(event_2));

        final Eventsource eventsource = subscription.getEventsource();

        assertThat(eventsource.getName(), is(eventSourceName));

        final Location location = eventsource.getLocation();

        assertThat(location.getJmsUri(), is(jmsUri));
        assertThat(location.getRestUri(), is(nullValue()));
    }
}
