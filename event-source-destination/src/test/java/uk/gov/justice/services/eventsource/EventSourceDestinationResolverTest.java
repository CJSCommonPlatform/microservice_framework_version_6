package uk.gov.justice.services.eventsource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.jms.JmsDestinations;
import uk.gov.justice.services.messaging.context.ContextName;

import javax.jms.Destination;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventSourceDestinationResolverTest {

    private static final String NAME = "test.event.listener";

    @Mock
    private JmsDestinations jmsDestinations;

    @Mock
    private Destination destination;

    @Test
    public void shouldResolveDestination() {
        Mockito.when(jmsDestinations.getDestination(EVENT_LISTENER, ContextName.fromName(NAME))).thenReturn(destination);
        EventSourceDestinationResolver eventSourceDestinationResolver = new EventSourceDestinationResolver();
        eventSourceDestinationResolver.jmsDestinations = jmsDestinations;

        Destination actualDestination = eventSourceDestinationResolver.resolve(NAME);

        assertThat(actualDestination, equalTo(destination));
        verify(jmsDestinations).getDestination(EVENT_LISTENER, ContextName.fromName(NAME));
    }

}