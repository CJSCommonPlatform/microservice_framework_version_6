package uk.gov.justice.services.jmx.api.command;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IndexerCatchupCommandTest {

    @InjectMocks
    private IndexerCatchupCommand indexerCatchupCommand;

    @Test
    public void shouldNotBeEventCatchupCommand() throws Exception {

        assertThat(indexerCatchupCommand.isEventCatchup(), is(false));
    }
}
