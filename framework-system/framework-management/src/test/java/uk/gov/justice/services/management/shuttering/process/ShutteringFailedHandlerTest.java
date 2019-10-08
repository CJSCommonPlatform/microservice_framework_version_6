package uk.gov.justice.services.management.shuttering.process;

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.command.UnshutterCommand;
import uk.gov.justice.services.management.shuttering.api.ShutteringExecutor;
import uk.gov.justice.services.management.shuttering.api.ShutteringResult;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class ShutteringFailedHandlerTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private ShutteringFailedHandler shutteringFailedHandler;

    @Test
    public void shouldLogErrorAndReturnAFailureResult() throws Exception {

        final UUID commandId = UUID.randomUUID();
        final String shutteringExecutorName = "ShutteringExecutorName";

        final SystemCommand systemCommand = new UnshutterCommand();
        final ShutteringExecutor shutteringExecutor = mock(ShutteringExecutor.class);

        final NullPointerException nullPointerException = new NullPointerException("Ooops");

        when(shutteringExecutor.getName()).thenReturn(shutteringExecutorName);

        final ShutteringResult shutteringResult = shutteringFailedHandler.onShutteringFailed(
                commandId,
                systemCommand,
                shutteringExecutor,
                nullPointerException
        );

        assertThat(shutteringResult.getCommandId(), is(commandId));
        assertThat(shutteringResult.getCommandState(), is(COMMAND_FAILED));
        assertThat(shutteringResult.getMessage(), is("UNSHUTTER failed for ShutteringExecutorName. java.lang.NullPointerException: Ooops"));
        assertThat(shutteringResult.getShutteringExecutorName(), is(shutteringExecutorName));
        assertThat(shutteringResult.getSystemCommand(), is(systemCommand));
        assertThat(shutteringResult.getException(), is(of(nullPointerException)));

        verify(logger).error("UNSHUTTER failed for ShutteringExecutorName. java.lang.NullPointerException: Ooops", nullPointerException);
    }
}
