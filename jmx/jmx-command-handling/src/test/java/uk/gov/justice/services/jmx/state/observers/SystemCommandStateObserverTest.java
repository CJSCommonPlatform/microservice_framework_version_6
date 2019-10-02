package uk.gov.justice.services.jmx.state.observers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jmx.logging.MdcLogger;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SystemCommandStateObserverTest {

    @Mock
    private SystemCommandStateHandler systemCommandStateHandler;

    @Mock
    private MdcLogger mdcLogger;

    private Consumer<Runnable> testConsumer = Runnable::run;

    @InjectMocks
    private SystemCommandStateObserver systemCommandStateObserver;

    @Test
    public void shouldProcessSystemCommandInProgressEvent() throws Exception {

        final SystemCommandStateChangedEvent systemCommandStateChangedEvent = mock(SystemCommandStateChangedEvent.class);

        when(mdcLogger.mdcLoggerConsumer()).thenReturn(testConsumer);

        systemCommandStateObserver.onSystemCommandStateChanged(systemCommandStateChangedEvent);

        verify(systemCommandStateHandler).handleSystemCommandStateChanged(systemCommandStateChangedEvent);
    }
}
