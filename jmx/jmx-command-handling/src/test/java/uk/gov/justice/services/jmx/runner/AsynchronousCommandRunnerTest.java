package uk.gov.justice.services.jmx.runner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_RECEIVED;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.domain.SystemCommandStatus;
import uk.gov.justice.services.jmx.command.TestCommand;
import uk.gov.justice.services.jmx.state.observers.SystemCommandStateBean;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.enterprise.concurrent.ManagedExecutorService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AsynchronousCommandRunnerTest {

    @Mock
    private SystemCommandStateBean systemCommandStateBean;

    @Mock
    private ManagedExecutorService managedExecutorService;

    @Mock
    private SystemCommandRunner systemCommandRunner;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private AsynchronousCommandRunner asynchronousCommandRunner;

    @Captor
    private ArgumentCaptor<RunSystemCommandTask> runSystemCommandTaskCaptor;

    @Captor
    private ArgumentCaptor<SystemCommandStatus> systemCommandStatusCaptor;

    @Test
    public void shouldRunTheSystemCommandAsynchronously() throws Exception {

        final ZonedDateTime now = new UtcClock().now();
        final SystemCommand systemCommand = new TestCommand();

        when(clock.now()).thenReturn(now);

        final UUID commandId = asynchronousCommandRunner.run(systemCommand);

        final InOrder inOrder = inOrder(systemCommandStateBean, managedExecutorService);

        inOrder.verify(systemCommandStateBean).addSystemCommandState(systemCommandStatusCaptor.capture());
        inOrder.verify(managedExecutorService).submit(runSystemCommandTaskCaptor.capture());


        final SystemCommandStatus systemCommandStatus = systemCommandStatusCaptor.getValue();

        assertThat(systemCommandStatus.getSystemCommandName(), is(systemCommand.getName()));
        assertThat(systemCommandStatus.getStatusChangedAt(), is(now));
        assertThat(systemCommandStatus.getCommandState(), is(COMMAND_RECEIVED));
        assertThat(systemCommandStatus.getCommandId(), is(commandId));
        assertThat(systemCommandStatus.getMessage(), is("System Command TEST_COMMAND Received"));

        final RunSystemCommandTask runSystemCommandTask = runSystemCommandTaskCaptor.getValue();

        assertThat(getValueOfField(runSystemCommandTask, "systemCommandRunner", SystemCommandRunner.class), is(systemCommandRunner));
        assertThat(getValueOfField(runSystemCommandTask, "systemCommand", SystemCommand.class), is(systemCommand));
        assertThat(getValueOfField(runSystemCommandTask, "commandId", UUID.class), is(commandId));
    }

    @Test
    public void shouldCheckThatCommandIsSupported() throws Exception {

        final boolean supported = true;
        final SystemCommand systemCommand = new TestCommand();

        when(systemCommandRunner.isSupported(systemCommand)).thenReturn(supported);

        assertThat(asynchronousCommandRunner.isSupported(systemCommand), is(supported));
    }
}
