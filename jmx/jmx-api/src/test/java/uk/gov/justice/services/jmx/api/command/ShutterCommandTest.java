package uk.gov.justice.services.jmx.api.command;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ShutterCommandTest {

    @InjectMocks
    private ShutterCommand shutterCommand;

    @Test
    public void shouldNotBeUnshutterCommand() throws Exception {

        assertThat(shutterCommand.isUnshuttering(), is(false));
    }
}
