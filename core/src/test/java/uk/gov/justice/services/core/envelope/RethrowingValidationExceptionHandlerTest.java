package uk.gov.justice.services.core.envelope;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.exceptions.base.MockitoException;


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