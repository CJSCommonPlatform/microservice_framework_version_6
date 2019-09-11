package uk.gov.justice.services.jmx.runner;

import static org.mockito.Mockito.verify;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class RunSystemCommandTaskTest {

    @Mock
    private SystemCommandRunner systemCommandRunner;

    @Mock
    private SystemCommand systemCommand;

    @InjectMocks
    private RunSystemCommandTask runSystemCommandTask;

    @Test
    public void shouldRunTheSystemCommand() throws Exception {
        
        runSystemCommandTask.call();

        verify(systemCommandRunner).run(systemCommand);
    }
}
