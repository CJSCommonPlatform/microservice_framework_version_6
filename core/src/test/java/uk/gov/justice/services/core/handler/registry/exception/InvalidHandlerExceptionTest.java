package uk.gov.justice.services.core.handler.registry.exception;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class InvalidHandlerExceptionTest {

    @Test
    public void shouldCreateInstanceOfInvalidHandlerExceptionnWithMessage() throws Exception {
        final InvalidHandlerException exception = new InvalidHandlerException("Test message");
        assertThat(exception.getMessage(), is("Test message"));
        assertThat(exception, instanceOf(RuntimeException.class));
    }
}