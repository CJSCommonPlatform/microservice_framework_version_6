package uk.gov.justice.services.management.shuttering.process;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.management.shuttering.api.ShutteringResult;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ShutteringResultsMapperTest {

    @InjectMocks
    private ShutteringResultsMapper shutteringResultsMapper;

    @Test
    public void shouldGetTheSubListOfSuccessfulResults() throws Exception {

        final ShutteringResult shutteringResult_1 = mock(ShutteringResult.class);
        final ShutteringResult shutteringResult_2 = mock(ShutteringResult.class);
        final ShutteringResult shutteringResult_3 = mock(ShutteringResult.class);
        final ShutteringResult shutteringResult_4 = mock(ShutteringResult.class);

        when(shutteringResult_1.getCommandState()).thenReturn(COMMAND_FAILED);
        when(shutteringResult_2.getCommandState()).thenReturn(COMMAND_COMPLETE);
        when(shutteringResult_3.getCommandState()).thenReturn(COMMAND_IN_PROGRESS);
        when(shutteringResult_4.getCommandState()).thenReturn(COMMAND_COMPLETE);

        final List<ShutteringResult> successfulResults = shutteringResultsMapper.getSuccessfulResults(asList(
                shutteringResult_1,
                shutteringResult_2,
                shutteringResult_3,
                shutteringResult_4));

        assertThat(successfulResults.size(), is(2));
        assertThat(successfulResults, hasItem(shutteringResult_2));
        assertThat(successfulResults, hasItem(shutteringResult_4));
    }

    @Test
    public void shouldGetTheSubListOfFailedResults() throws Exception {

        final ShutteringResult shutteringResult_1 = mock(ShutteringResult.class);
        final ShutteringResult shutteringResult_2 = mock(ShutteringResult.class);
        final ShutteringResult shutteringResult_3 = mock(ShutteringResult.class);
        final ShutteringResult shutteringResult_4 = mock(ShutteringResult.class);

        when(shutteringResult_1.getCommandState()).thenReturn(COMMAND_FAILED);
        when(shutteringResult_2.getCommandState()).thenReturn(COMMAND_COMPLETE);
        when(shutteringResult_3.getCommandState()).thenReturn(COMMAND_FAILED);
        when(shutteringResult_4.getCommandState()).thenReturn(COMMAND_IN_PROGRESS);

        final List<ShutteringResult> failedResults = shutteringResultsMapper.getFailedResults(asList(
                shutteringResult_1,
                shutteringResult_2,
                shutteringResult_3,
                shutteringResult_4));

        assertThat(failedResults.size(), is(2));
        assertThat(failedResults, hasItem(shutteringResult_1));
        assertThat(failedResults, hasItem(shutteringResult_3));
    }

    @Test
    public void shouldGetAListOfShutteringExecutorNames() throws Exception {

        final ShutteringResult shutteringResult_1 = mock(ShutteringResult.class);
        final ShutteringResult shutteringResult_2 = mock(ShutteringResult.class);
        final ShutteringResult shutteringResult_3 = mock(ShutteringResult.class);
        final ShutteringResult shutteringResult_4 = mock(ShutteringResult.class);

        when(shutteringResult_1.getShutteringExecutorName()).thenReturn("Executor name 1");
        when(shutteringResult_2.getShutteringExecutorName()).thenReturn("Executor name 2");
        when(shutteringResult_3.getShutteringExecutorName()).thenReturn("Executor name 3");
        when(shutteringResult_4.getShutteringExecutorName()).thenReturn("Executor name 4");

        final List<String> shutteringExecutorNames = shutteringResultsMapper.getShutteringExecutorNames(asList(
                shutteringResult_1,
                shutteringResult_2,
                shutteringResult_3,
                shutteringResult_4));

        assertThat(shutteringExecutorNames.size(), is(4));
        assertThat(shutteringExecutorNames, hasItem("Executor name 1"));
        assertThat(shutteringExecutorNames, hasItem("Executor name 2"));
        assertThat(shutteringExecutorNames, hasItem("Executor name 3"));
        assertThat(shutteringExecutorNames, hasItem("Executor name 4"));
    }
}
