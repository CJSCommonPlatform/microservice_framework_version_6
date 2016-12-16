package uk.gov.justice.services.core.handler.exception;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class MissingHandlerExceptionTest {

    @Test
    public void shouldCreateInstanceOfMissingHandlerExceptionWithMessage() throws Exception {
        final MissingHandlerException exception = new MissingHandlerException("Test message");
        assertThat(exception.getMessage(), is("Test message"));
        assertThat(exception, instanceOf(RuntimeException.class));
    }
}