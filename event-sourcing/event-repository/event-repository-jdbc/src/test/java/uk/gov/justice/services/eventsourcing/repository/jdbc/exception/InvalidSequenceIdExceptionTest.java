package uk.gov.justice.services.eventsourcing.repository.jdbc.exception;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class InvalidSequenceIdExceptionTest {

    @Test
    public void shouldCreateInstanceOfInvalidSequenceIdExceptionWithMessage() throws Exception {
        final InvalidSequenceIdException exception = new InvalidSequenceIdException("Test message");
        assertThat(exception.getMessage(), is("Test message"));
        assertThat(exception, instanceOf(Exception.class));
    }
}