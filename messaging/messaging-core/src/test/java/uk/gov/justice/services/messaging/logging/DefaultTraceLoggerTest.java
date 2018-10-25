package uk.gov.justice.services.messaging.logging;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.mockito.internal.matchers.Any;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.util.function.Supplier;

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
