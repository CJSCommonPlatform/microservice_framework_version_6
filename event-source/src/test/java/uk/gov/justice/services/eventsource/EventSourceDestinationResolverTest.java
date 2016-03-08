package uk.gov.justice.services.eventsource;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.jms.JmsEndpoints;
import uk.gov.justice.services.messaging.context.ContextName;

import javax.jms.Destination;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceDestinationResolverTest {

    private static final String NAME = "test.events.listener";

    @Mock
    private JmsEndpoints jmsEndpoints;

    @Mock
    private Destination destination;

    @Test
    public void shouldResolveDestination() {
        Mockito.when(jmsEndpoints.getEndpoint(EVENT_LISTENER, ContextName.fromName(NAME))).thenReturn(destination);
        EventSourceDestinationResolver eventSourceDestinationResolver = new EventSourceDestinationResolver();
        eventSourceDestinationResolver.jmsEndpoints = jmsEndpoints;

        Destination actualDestination = eventSourceDestinationResolver.resolve(NAME);

        Assert.assertThat(actualDestination, CoreMatchers.equalTo(destination));
        Mockito.verify(jmsEndpoints).getEndpoint(EVENT_LISTENER, ContextName.fromName(NAME));
    }

}