package uk.gov.justice.services.jmx.command;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommandDetails;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CommandConverterTest {

    @InjectMocks
    private CommandConverter commandConverter;

    @Test
    public void shouldConvertSystemCommandToSystemCommandDetails() throws Exception {

        final SystemCommand systemCommand = new TestCommand();

        final SystemCommandDetails systemCommandDetails = commandConverter.toCommandDetails(systemCommand);

        assertThat(systemCommandDetails.getName(), is(systemCommand.getName()));
        assertThat(systemCommandDetails.getDescription(), is(systemCommand.getDescription()));
    }
}
