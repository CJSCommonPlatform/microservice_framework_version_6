package uk.gov.justice.services.jmx.state.observers;

import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        when(systemCommandStatusRepository.findLatestStatus(commandId)).thenReturn(systemCommandStatus);

        assertThat(systemCommandStateBean.getCommandStatus(commandId), is(systemCommandStatus));
    }
}
