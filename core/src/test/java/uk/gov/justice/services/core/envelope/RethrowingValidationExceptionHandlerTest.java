package uk.gov.justice.services.core.envelope;

import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class RethrowingValidationExceptionHandlerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowHandlerException() throws Exception {

        exception.expect(EnvelopeValidationException.class);
        exception.expectMessage(equalTo("some message"));

        new RethrowingValidationExceptionHandler().handle(new EnvelopeValidationException("some message"));

    }
}
