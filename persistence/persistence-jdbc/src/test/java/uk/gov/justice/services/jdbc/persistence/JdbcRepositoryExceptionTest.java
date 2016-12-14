package uk.gov.justice.services.jdbc.persistence;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class JdbcRepositoryExceptionTest {

    @Test
    public void shouldCreateInstanceOfJdbcRepositoryExceptionnWithMessage() throws Exception {
        final JdbcRepositoryException exception = new JdbcRepositoryException("Test message");
        assertThat(exception.getMessage(), is("Test message"));
        assertThat(exception, instanceOf(RuntimeException.class));
    }

    @Test
    public void shouldCreateInstanceOfJdbcRepositoryExceptionnWithMessageAndCause() throws Exception {
        final Throwable throwable = mock(Throwable.class);
        final JdbcRepositoryException exception = new JdbcRepositoryException("Test message", throwable);
        assertThat(exception.getMessage(), is("Test message"));
        assertThat(exception.getCause(), is(throwable));
    }

    @Test
    public void shouldCreateInstanceOfJdbcRepositoryExceptionnWithCause() throws Exception {
        final Throwable throwable = mock(Throwable.class);
        final JdbcRepositoryException exception = new JdbcRepositoryException(throwable);
        assertThat(exception.getCause(), is(throwable));
    }
}