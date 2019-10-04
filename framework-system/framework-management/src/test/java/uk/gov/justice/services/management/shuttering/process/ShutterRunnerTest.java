package uk.gov.justice.services.management.shuttering.process;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;

import org.junit.runner.RunWith;
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

import uk.gov.justice.services.jmx.api.command.ShutterCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.api.ShutteringExecutor;

import java.util.UUID;

import javax.inject.Inject;

@RunWith(MockitoJUnitRunner.class)
public class ShutterRunnerTest {

    @Mock
    private ShutteringExecutorProvider shutteringExecutorProvider;

    @Mock
    private Logger logger;

    @InjectMocks
    private ShutterRunner shutterRunner;

    @Test
    public void shouldRunShutteringOnShutteringExecutorIfTheExecutorSupportsIt() throws Exception {

        final UUID commandId = randomUUID();
        final SystemCommand systemCommand = new ShutterCommand();

        final ShutteringExecutor shutteringExecutor_1 = mock(ShutteringExecutor.class);
        final ShutteringExecutor shutteringExecutor_2 = mock(ShutteringExecutor.class);
        final ShutteringExecutor shutteringExecutor_3 = mock(ShutteringExecutor.class);
        final ShutteringExecutor shutteringExecutor_4 = mock(ShutteringExecutor.class);

        when(shutteringExecutorProvider.getShutteringExecutors()).thenReturn(asList(
                shutteringExecutor_1,
                shutteringExecutor_2,
                shutteringExecutor_3,
                shutteringExecutor_4
        ));

        when(shutteringExecutor_1.shouldShutter()).thenReturn(true);
        when(shutteringExecutor_3.shouldShutter()).thenReturn(true);

        when(shutteringExecutor_1.getName()).thenReturn("Executor 1");
        when(shutteringExecutor_3.getName()).thenReturn("Executor 3");

        shutterRunner.runShuttering(commandId, systemCommand);

        verify(logger).info("Shuttering Executor 1");
        verify(shutteringExecutor_1).shutter(commandId, systemCommand);
        verify(logger).info("Shuttering Executor 3");
        verify(shutteringExecutor_3).shutter(commandId, systemCommand);

        verify(shutteringExecutor_2, never()).shutter(commandId, systemCommand);
        verify(shutteringExecutor_4, never()).shutter(commandId, systemCommand);
    }
}
