package uk.gov.justice.services.jmx.state.observers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.jmx.state.domain.SystemCommandStatus;
import uk.gov.justice.services.jmx.state.persistence.SystemCommandStatusRepository;

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
}
