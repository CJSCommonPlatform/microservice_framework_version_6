package uk.gov.justice.services.core.jms;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.jms.exception.JmsSenderException;

import javax.jms.Destination;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JmsDestinationsTest {

    private static final String CONTEXT_NAME = "test";
    private static final String CONTROLLER_ENDPOINT = "test.controller.command";
    private static final String HANDLER_ENDPOINT = "test.handler.command";
    private static final String LISTENER_ENDPOINT = "test.event";

    @Mock
    Context initialContext;

    @Mock
    Destination destination;

    private DefaultJmsDestinations jmsDestinations;

    @Before
    public void setup() throws NamingException {
        jmsDestinations = new DefaultJmsDestinations();
        jmsDestinations.initialContext = initialContext;
    }

    @Test
    public void shouldReturnCommandControllerEndpoint() throws Exception {
        when(initialContext.lookup(CONTROLLER_ENDPOINT)).thenReturn(destination);

        Destination actualDestination = jmsDestinations.getDestination(Component.COMMAND_CONTROLLER, CONTEXT_NAME);

        assertThat(actualDestination, equalTo(destination));
        verify(initialContext).lookup(CONTROLLER_ENDPOINT);
    }


    @Test
    public void shouldReturnCommandHandlerEndPoint() throws Exception {
        when(initialContext.lookup(HANDLER_ENDPOINT)).thenReturn(destination);

        Destination actualDestination = jmsDestinations.getDestination(COMMAND_HANDLER, CONTEXT_NAME);

        assertThat(actualDestination, equalTo(destination));
        verify(initialContext).lookup(HANDLER_ENDPOINT);
    }

    @Test
    public void shouldReturnEventListenerEndPoint() throws Exception {
        when(initialContext.lookup(LISTENER_ENDPOINT)).thenReturn(destination);

        Destination actualDestination = jmsDestinations.getDestination(EVENT_LISTENER, CONTEXT_NAME);

        assertThat(actualDestination, equalTo(destination));
        verify(initialContext).lookup(LISTENER_ENDPOINT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNonExistentEndpoint() throws Exception {
        jmsDestinations.getDestination(Component.COMMAND_API, CONTEXT_NAME);
    }

    @Test(expected = JmsSenderException.class)
    public void shouldThrowExceptionOnNonExistentJndiName() throws Exception {
        doThrow(NameNotFoundException.class).when(initialContext).lookup(anyString());

        jmsDestinations.getDestination(Component.COMMAND_CONTROLLER, CONTEXT_NAME);
    }

}