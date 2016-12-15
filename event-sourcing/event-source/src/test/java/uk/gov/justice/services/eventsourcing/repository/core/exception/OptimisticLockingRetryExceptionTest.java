package uk.gov.justice.services.eventsourcing.repository.core.exception;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;

public class OptimisticLockingRetryExceptionTest {

    @Test
    public void shouldCreateRuntimeException() throws Exception {
        final OptimisticLockingRetryException exception = new OptimisticLockingRetryException("Test");

        assertThat(exception, Matchers.instanceOf(RuntimeException.class));
        assertThat(exception.getMessage(), is("Test"));
    }
}