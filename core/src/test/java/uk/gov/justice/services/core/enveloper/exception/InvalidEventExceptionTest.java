package uk.gov.justice.services.core.enveloper.exception;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class InvalidEventExceptionTest {

    @Test
    public void shouldCreateInstanceOfInvalidEventExceptionWithMessage() throws Exception {
        final InvalidEventException exception = new InvalidEventException("Test message");
        assertThat(exception.getMessage(), is("Test message"));
        assertThat(exception, instanceOf(RuntimeException.class));
    }
}