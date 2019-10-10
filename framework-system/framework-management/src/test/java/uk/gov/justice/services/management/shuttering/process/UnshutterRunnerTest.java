package uk.gov.justice.services.management.shuttering.process;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jmx.api.command.ApplicationShutteringCommand;
import uk.gov.justice.services.jmx.api.command.ShutterCommand;
import uk.gov.justice.services.management.shuttering.api.ShutteringExecutor;
import uk.gov.justice.services.management.shuttering.api.ShutteringResult;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class UnshutterRunnerTest {

    @Mock
    private ShutteringExecutorProvider shutteringExecutorProvider;

    @Mock
    private ShutteringFailedHandler shutteringFailedHandler;

    @Mock
    private Logger logger;

    @InjectMocks
    private UnshutterRunner unshutterRunner;

    @Test
    public void shouldRunUnshutteringOnShutteringExecutorIfTheExecutorSupportsIt() throws Exception {

        final UUID commandId = randomUUID();
        final ApplicationShutteringCommand applicationShutteringCommand = new ShutterCommand();

        final ShutteringExecutor shutteringExecutor_1 = mock(ShutteringExecutor.class);
        final ShutteringExecutor shutteringExecutor_2 = mock(ShutteringExecutor.class);
        final ShutteringExecutor shutteringExecutor_3 = mock(ShutteringExecutor.class);
        final ShutteringExecutor shutteringExecutor_4 = mock(ShutteringExecutor.class);

        final ShutteringResult shutteringResult_1 = mock(ShutteringResult.class);
        final ShutteringResult shutteringResult_3 = mock(ShutteringResult.class);

        when(shutteringExecutorProvider.getShutteringExecutors()).thenReturn(asList(
                shutteringExecutor_1,
                shutteringExecutor_2,
                shutteringExecutor_3,
                shutteringExecutor_4
        ));

        when(shutteringExecutor_1.shouldUnshutter()).thenReturn(true);
        when(shutteringExecutor_3.shouldUnshutter()).thenReturn(true);

        when(shutteringExecutor_1.getName()).thenReturn("Executor 1");
        when(shutteringExecutor_3.getName()).thenReturn("Executor 3");

        when(shutteringExecutor_1.unshutter(commandId, applicationShutteringCommand)).thenReturn(shutteringResult_1);
        when(shutteringExecutor_3.unshutter(commandId, applicationShutteringCommand)).thenReturn(shutteringResult_3);


        final List<ShutteringResult> shutteringResults = unshutterRunner.runUnshuttering(commandId, applicationShutteringCommand);

        assertThat(shutteringResults.size(), is(2));
        assertThat(shutteringResults, hasItem(shutteringResult_1));
        assertThat(shutteringResults, hasItem(shutteringResult_3));

        verify(logger).info("Unshuttering Executor 1");
        verify(logger).info("Unshuttering Executor 3");
        verify(shutteringExecutor_2, never()).unshutter(commandId, applicationShutteringCommand);
        verify(shutteringExecutor_4, never()).unshutter(commandId, applicationShutteringCommand);
    }

    @Test
    public void shouldHandleShutteringExceptions() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException("Ooops");

        final UUID commandId = randomUUID();
        final ApplicationShutteringCommand applicationShutteringCommand = new ShutterCommand();

        final ShutteringExecutor shutteringExecutor_1 = mock(ShutteringExecutor.class);
        final ShutteringExecutor shutteringExecutor_2 = mock(ShutteringExecutor.class);
        final ShutteringExecutor shutteringExecutor_3 = mock(ShutteringExecutor.class);
        final ShutteringExecutor shutteringExecutor_4 = mock(ShutteringExecutor.class);

        final ShutteringResult failureResult = mock(ShutteringResult.class);
        final ShutteringResult shutteringResult_3 = mock(ShutteringResult.class);

        when(shutteringExecutorProvider.getShutteringExecutors()).thenReturn(asList(
                shutteringExecutor_1,
                shutteringExecutor_2,
                shutteringExecutor_3,
                shutteringExecutor_4
        ));

        when(shutteringExecutor_1.shouldUnshutter()).thenReturn(true);
        when(shutteringExecutor_3.shouldUnshutter()).thenReturn(true);

        when(shutteringExecutor_1.getName()).thenReturn("Executor 1");
        when(shutteringExecutor_3.getName()).thenReturn("Executor 3");

        when(shutteringExecutor_1.unshutter(commandId, applicationShutteringCommand)).thenThrow(nullPointerException);
        when(shutteringExecutor_3.unshutter(commandId, applicationShutteringCommand)).thenReturn(shutteringResult_3);

        when(shutteringFailedHandler.onShutteringFailed(
                commandId,
                applicationShutteringCommand,
                shutteringExecutor_1,
                nullPointerException)
        ).thenReturn(failureResult);

        final List<ShutteringResult> shutteringResults = unshutterRunner.runUnshuttering(commandId, applicationShutteringCommand);

        assertThat(shutteringResults.size(), is(2));
        assertThat(shutteringResults, hasItem(failureResult));
        assertThat(shutteringResults, hasItem(shutteringResult_3));
    }
}
