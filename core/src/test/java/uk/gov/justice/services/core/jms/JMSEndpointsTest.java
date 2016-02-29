package uk.gov.justice.services.core.jms;

import org.junit.Test;
import uk.gov.justice.services.core.annotation.Component;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class JmsEndpointsTest {

    private static final String CONTEXT_NAME = "hello";

    @Test
    public void shouldReturnCommandControllerEndpoint() throws Exception {
        assertThat(new JmsEndpoints().getEndpoint(Component.COMMAND_CONTROLLER, CONTEXT_NAME), equalTo("hello.controller.commands"));
    }

    @Test
    public void shouldReturnCommandHandlerEndPoint() throws Exception {
        assertThat(new JmsEndpoints().getEndpoint(Component.COMMAND_HANDLER, CONTEXT_NAME), equalTo("hello.handler.commands"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNonExistentEndpoint() throws Exception {
        new JmsEndpoints().getEndpoint(Component.COMMAND_API, CONTEXT_NAME);
    }
}