package uk.gov.justice.services.messaging.spi;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class JsonEnvelopeProviderNotFoundExceptionTest {

    @Test
    public void shouldCreateExceptionWithMessage() throws Exception {
        final JsonEnvelopeProviderNotFoundException exception = new JsonEnvelopeProviderNotFoundException("Test message");
        assertThat(exception.getMessage(), is("Test message"));
    }
}