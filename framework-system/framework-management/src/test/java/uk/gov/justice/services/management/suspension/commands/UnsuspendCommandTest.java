package uk.gov.justice.services.management.suspension.commands;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UnsuspendCommandTest {

    @InjectMocks
    private UnsuspendCommand unsuspendCommand;

    @Test
    public void shouldBeUnsuspendCommand() throws Exception {

        assertThat(unsuspendCommand.isSuspendCommand(), is(false));
    }
}
