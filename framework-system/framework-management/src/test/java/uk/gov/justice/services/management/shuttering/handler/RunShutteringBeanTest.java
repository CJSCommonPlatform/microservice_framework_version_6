package uk.gov.justice.services.management.shuttering.handler;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.logging.MdcLogger;
import uk.gov.justice.services.management.shuttering.process.ShutteringProcessRunner;

import java.util.UUID;
import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RunShutteringBeanTest {

    @Mock
    private MdcLogger mdcLogger;

    @Mock
    private ShutteringProcessRunner shutteringProcessRunner;

    @InjectMocks
    private RunShutteringBean runShutteringBean;

    private Consumer<Runnable> testConsumer = Runnable::run;

    @Test
    public void shouldRunShutteringInMdcLogging() throws Exception {

        final UUID commandId = randomUUID();
        final SystemCommand systemCommand = mock(SystemCommand.class);

        when(mdcLogger.mdcLoggerConsumer()).thenReturn(testConsumer);

        runShutteringBean.runShuttering(commandId, systemCommand);

        verify(shutteringProcessRunner).runShuttering(commandId, systemCommand);
    }
}
