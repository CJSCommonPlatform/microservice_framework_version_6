package uk.gov.justice.services.jmx.bootstrap.blacklist;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EmptyBlackListedCommandsTest {

    @InjectMocks
    private EmptyBlackListedCommands emptyBlackListedCommands;

    @Test
    public void shouldReturnEmptyByDefault() throws Exception {
        assertThat(emptyBlackListedCommands.getBlackListedCommands().isEmpty(), is(true));
    }
}
