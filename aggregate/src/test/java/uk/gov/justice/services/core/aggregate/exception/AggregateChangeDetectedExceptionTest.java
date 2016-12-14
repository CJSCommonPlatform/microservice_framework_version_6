package uk.gov.justice.services.core.aggregate.exception;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class AggregateChangeDetectedExceptionTest {

    @Test
    public void shouldCreateInstanceOfAggregateChangeDetectedExceptionWithMessage() throws Exception {
        final AggregateChangeDetectedException exception = new AggregateChangeDetectedException("Test message");
        assertThat(exception.getMessage(), is("Test message"));
        assertThat(exception, instanceOf(Exception.class));
    }
}