package uk.gov.justice.services.messaging.logging;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DebugLoggerTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private DebugLogger debugLogger;

    @Test
    public void shouldLogNameIfDebugIsTrue() {

        when(logger.isDebugEnabled()).thenReturn(true);

        debugLogger.debug(logger, () -> "Test Message");

        verify(logger).debug("Test Message");
    }

    @Test
    public void shouldNotLogNameIfDebugIsFalse() {

        when(logger.isDebugEnabled()).thenReturn(false);

        debugLogger.debug(logger, () -> "Test Message");

        verify(logger).isDebugEnabled();
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void shouldLogErrorIfExceptionIsThrownWhenLogging() {

        final RuntimeException runtimeException = new RuntimeException();

        when(logger.isDebugEnabled()).thenReturn(true);

        debugLogger.debug(logger, () -> {
            throw runtimeException;
        });

        verify(logger).isDebugEnabled();
        verify(logger).error("Could not generate debug log message", runtimeException);
    }
}