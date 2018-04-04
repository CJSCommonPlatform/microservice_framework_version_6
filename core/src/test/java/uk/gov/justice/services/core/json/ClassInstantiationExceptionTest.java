package uk.gov.justice.services.core.json;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ClassInstantiationExceptionTest {

    @Test
    public void shouldCreateExceptionWithMessage() throws Exception {
        final ClassInstantiationException exception = new ClassInstantiationException("Test message", new Throwable());
        assertThat(exception.getMessage(), is("Test message"));
    }

}
