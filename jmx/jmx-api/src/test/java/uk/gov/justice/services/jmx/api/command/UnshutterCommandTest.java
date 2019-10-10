package uk.gov.justice.services.jmx.api.command;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class UnshutterCommandTest {

    @InjectMocks
    private UnshutterCommand unshutterCommand;

    @Test
    public void shouldBeUnshutterCommand() throws Exception {

        assertThat(unshutterCommand.isUnshuttering(), is(true));
    }
}
