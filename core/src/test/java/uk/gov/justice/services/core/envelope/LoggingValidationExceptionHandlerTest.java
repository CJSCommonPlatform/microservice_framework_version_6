package uk.gov.justice.services.core.envelope;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.json.DefaultJsonValidationLoggerHelper;

import org.everit.json.schema.ValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class LoggingValidationExceptionHandlerTest {

    @Mock
    private Logger logger;

    @Mock
    private DefaultJsonValidationLoggerHelper defaultJsonValidationLoggerHelper;

    @InjectMocks
    private LoggingValidationExceptionHandler exceptionHandler;

    @Test
    public void shouldLogException() throws Exception {
        final EnvelopeValidationException exception = new EnvelopeValidationException("");
        when(logger.isWarnEnabled()).thenReturn(true);
        exceptionHandler.handle(exception);
        verify(logger).warn("Message validation failed ", exception);
    }

    @Test
    public void shouldLogValidationException() throws Exception {
        final ValidationException validationException = mock(ValidationException.class);
        final EnvelopeValidationException exception = new EnvelopeValidationException("Help me", validationException);
        when(logger.isWarnEnabled()).thenReturn(true);
        when(defaultJsonValidationLoggerHelper.toValidationTrace(validationException)).thenReturn("string");
        exceptionHandler.handle(exception);
        verify(logger).warn("Message validation failed string", exception);
    }

}