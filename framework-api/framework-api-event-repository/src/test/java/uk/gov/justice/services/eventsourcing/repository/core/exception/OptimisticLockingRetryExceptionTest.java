package uk.gov.justice.services.eventsourcing.repository.core.exception;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class OptimisticLockingRetryExceptionTest {

    @Test
    public void shouldCreateRuntimeException() throws Exception {
        final OptimisticLockingRetryException exception = new OptimisticLockingRetryException("Test");

        assertThat(exception, instanceOf(RuntimeException.class));
        assertThat(exception.getMessage(), is("Test"));
    }
}