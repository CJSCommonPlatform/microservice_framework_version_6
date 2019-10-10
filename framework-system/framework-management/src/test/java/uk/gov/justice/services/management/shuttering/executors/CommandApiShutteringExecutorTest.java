package uk.gov.justice.services.management.shuttering.executors;

import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;

import uk.gov.justice.services.jmx.api.command.ApplicationShutteringCommand;
import uk.gov.justice.services.management.shuttering.api.ShutteringResult;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class CommandApiShutteringExecutorTest {

    @Mock
    private CommandApiShutteringBean commandApiShutteringBean;

    @Mock
    private Logger logger;

    @InjectMocks
    private CommandApiShutteringExecutor commandApiShutteringExecutor;

    @Test
    public void shouldRunShuttering() throws Exception {

        assertThat(commandApiShutteringExecutor.shouldShutter(), is(true));
    }

    @Test
    public void shouldRunUnshuttering() throws Exception {

        assertThat(commandApiShutteringExecutor.shouldUnshutter(), is(true));
    }

    @Test
    public void shouldCallTheShutteringBeanAndShutter() throws Exception {

        final UUID commandId = randomUUID();
        final ApplicationShutteringCommand applicationShutteringCommand = mock(ApplicationShutteringCommand.class);

        final ShutteringResult shutteringResult = commandApiShutteringExecutor.shutter(commandId, applicationShutteringCommand);

        assertThat(shutteringResult.getCommandId(), is(commandId));
        assertThat(shutteringResult.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(shutteringResult.getSystemCommand(), is(applicationShutteringCommand));
        assertThat(shutteringResult.getMessage(), is("Command API shuttered with no errors"));
        assertThat(shutteringResult.getShutteringExecutorName(), is("CommandApiShutteringExecutor"));
        assertThat(shutteringResult.getException(), is(empty()));

        final InOrder inOrder = inOrder(logger, commandApiShutteringBean);

        inOrder.verify(logger).info("Shuttering Command API");
        inOrder.verify(commandApiShutteringBean).shutter();
        inOrder.verify(logger).info("Shuttering of Command API complete");
    }

    @Test
    public void shouldCallTheShutteringBeanAndUnshutter() throws Exception {

        final UUID commandId = randomUUID();
        final ApplicationShutteringCommand applicationShutteringCommand = mock(ApplicationShutteringCommand.class);

        final ShutteringResult shutteringResult = commandApiShutteringExecutor.unshutter(commandId, applicationShutteringCommand);

        assertThat(shutteringResult.getCommandId(), is(commandId));
        assertThat(shutteringResult.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(shutteringResult.getSystemCommand(), is(applicationShutteringCommand));
        assertThat(shutteringResult.getMessage(), is("Command API unshuttered with no errors"));
        assertThat(shutteringResult.getShutteringExecutorName(), is("CommandApiShutteringExecutor"));
        assertThat(shutteringResult.getException(), is(empty()));

        final InOrder inOrder = inOrder(logger, commandApiShutteringBean);

        inOrder.verify(logger).info("Unshuttering Command API");
        inOrder.verify(commandApiShutteringBean).unshutter();
        inOrder.verify(logger).info("Unshuttering of Command API complete");
    }
}
