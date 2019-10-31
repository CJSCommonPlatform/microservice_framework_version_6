package uk.gov.justice.services.jmx.state.observers;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_RECEIVED;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.domain.CommandState;
import uk.gov.justice.services.jmx.api.domain.SystemCommandStatus;
import uk.gov.justice.services.jmx.state.persistence.SystemCommandStatusRepository;

import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SystemCommandStateBeanTest {

    @Mock
    private SystemCommandStatusRepository systemCommandStatusRepository;

    @InjectMocks
    private SystemCommandStateBean systemCommandStateBean;

    @Test
    public void shouldAddSystemCommandStatusToTheRepository() throws Exception {

        final SystemCommandStatus systemCommandStatus = mock(SystemCommandStatus.class);

        systemCommandStateBean.addSystemCommandState(systemCommandStatus);

        verify(systemCommandStatusRepository).add(systemCommandStatus);
    }

    @Test
    public void shouldGetSystemCommandStatusFromTheRepository() throws Exception {

        final UUID commandId = randomUUID();

        final Optional<SystemCommandStatus> systemCommandStatus = of(mock(SystemCommandStatus.class));

        when(systemCommandStatusRepository.findLatestStatusById(commandId)).thenReturn(systemCommandStatus);

        assertThat(systemCommandStateBean.getCommandStatus(commandId), is(systemCommandStatus));
    }

    @Test
    public void shouldDetermineIfACommandIsInProgress() throws Exception {

        final SystemCommand commandReceivedCommand = mock(SystemCommand.class);
        final SystemCommand commandInProgressCommand = mock(SystemCommand.class);
        final SystemCommand commandCompleteCommand = mock(SystemCommand.class);
        final SystemCommand commandFailedCommand = mock(SystemCommand.class);
        final SystemCommand notFoundCommand = mock(SystemCommand.class);

        final SystemCommandStatus commandReceivedStatus = mock(SystemCommandStatus.class);
        final SystemCommandStatus commandInProgressStatus = mock(SystemCommandStatus.class);
        final SystemCommandStatus commandCompleteStatus = mock(SystemCommandStatus.class);
        final SystemCommandStatus commandFailedStatus = mock(SystemCommandStatus.class);

        when(systemCommandStatusRepository.findLatestStatusByType(commandReceivedCommand)).thenReturn(of(commandReceivedStatus));
        when(systemCommandStatusRepository.findLatestStatusByType(commandInProgressCommand)).thenReturn(of(commandInProgressStatus));
        when(systemCommandStatusRepository.findLatestStatusByType(commandCompleteCommand)).thenReturn(of(commandCompleteStatus));
        when(systemCommandStatusRepository.findLatestStatusByType(commandFailedCommand)).thenReturn(of(commandFailedStatus));
        when(systemCommandStatusRepository.findLatestStatusByType(notFoundCommand)).thenReturn(empty());

        when(commandReceivedStatus.getCommandState()).thenReturn(COMMAND_RECEIVED);
        when(commandInProgressStatus.getCommandState()).thenReturn(COMMAND_IN_PROGRESS);
        when(commandCompleteStatus.getCommandState()).thenReturn(COMMAND_COMPLETE);
        when(commandFailedStatus.getCommandState()).thenReturn(COMMAND_FAILED);

        assertThat(systemCommandStateBean.commandInProgress(commandReceivedCommand), is(true));
        assertThat(systemCommandStateBean.commandInProgress(commandInProgressCommand), is(true));
        assertThat(systemCommandStateBean.commandInProgress(commandCompleteCommand), is(false));
        assertThat(systemCommandStateBean.commandInProgress(commandFailedCommand), is(false));
        assertThat(systemCommandStateBean.commandInProgress(notFoundCommand), is(false));
    }
}
