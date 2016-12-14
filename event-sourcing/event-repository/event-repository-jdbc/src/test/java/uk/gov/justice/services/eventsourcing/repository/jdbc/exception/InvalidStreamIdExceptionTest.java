package uk.gov.justice.services.eventsourcing.repository.jdbc.exception;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class InvalidStreamIdExceptionTest {

    @Test
    public void shouldCreateInstanceOfInvalidStreamIdExceptionWithMessage() throws Exception {
        final InvalidStreamIdException exception = new InvalidStreamIdException("Test message");
        assertThat(exception.getMessage(), is("Test message"));
        assertThat(exception, instanceOf(RuntimeException.class));
    }
}