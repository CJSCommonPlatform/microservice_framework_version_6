package uk.gov.justice.services.jmx.command;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jmx.api.command.SystemCommand;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SystemCommandLocatorTest {

    @Mock
    private SystemCommandScanner systemCommandScanner;
    
    @InjectMocks
    private SystemCommandLocator systemCommandLocator;

    @Test
    public void shouldFindCommandByName() throws Exception {

        final SystemCommand systemCommand_1 = mock(SystemCommand.class);
        final SystemCommand systemCommand_2 = mock(SystemCommand.class);
        final SystemCommand systemCommand_3 = mock(SystemCommand.class);

        when(systemCommand_1.getName()).thenReturn("command_1");
        when(systemCommand_2.getName()).thenReturn("command_2");
        when(systemCommand_3.getName()).thenReturn("command_3");

        when(systemCommandScanner.findCommands()).thenReturn(asList(
                systemCommand_1,
                systemCommand_2,
                systemCommand_3));

        assertThat(systemCommandLocator.forName("command_2"), is(of(systemCommand_2)));
    }


    @Test
    public void shouldReturnEmptyIfNoCommandFound() throws Exception {

        final SystemCommand systemCommand_1 = mock(SystemCommand.class);
        final SystemCommand systemCommand_2 = mock(SystemCommand.class);
        final SystemCommand systemCommand_3 = mock(SystemCommand.class);

        when(systemCommand_1.getName()).thenReturn("command_1");
        when(systemCommand_2.getName()).thenReturn("command_2");
        when(systemCommand_3.getName()).thenReturn("command_3");

        when(systemCommandScanner.findCommands()).thenReturn(asList(
                systemCommand_1,
                systemCommand_2,
                systemCommand_3));

        assertThat(systemCommandLocator.forName("other_command"), is(empty()));
    }

}
