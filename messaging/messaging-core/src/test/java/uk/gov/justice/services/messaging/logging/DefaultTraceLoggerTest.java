package uk.gov.justice.services.messaging.logging;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.Test;
import org.slf4j.Logger;

@SuppressWarnings("unchecked")
public class DefaultTraceLoggerTest {

    private DefaultTraceLogger defaultTraceLogger = new DefaultTraceLogger();

    @Test
    public void shouldLogAMessageSuppliedByASupplier() throws Exception {

        final String logMessage = "A log message";

        final Logger logger = mock(Logger.class);
        final Supplier<String>  stringSupplier = mock(Supplier.class);

        when(logger.isTraceEnabled()).thenReturn(true);
        when(stringSupplier.get()).thenReturn(logMessage);

        defaultTraceLogger.trace(logger, stringSupplier);

        verify(logger).trace(logMessage);
    }

    @Test
    public void shouldDoNothingIfTraceIsNotEnabled() throws Exception {

        final Logger logger = mock(Logger.class);
        final Supplier<String>  stringSupplier = mock(Supplier.class);

        when(logger.isTraceEnabled()).thenReturn(false);

        defaultTraceLogger.trace(logger, stringSupplier);

        verify(logger, never()).trace(any(String.class));
        verifyZeroInteractions(stringSupplier);
    }


}
