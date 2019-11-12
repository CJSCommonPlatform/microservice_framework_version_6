package uk.gov.justice.services.management.suspension.process;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.management.suspension.api.SuspensionResult;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SuspensionResultsMapperTest {

    @InjectMocks
    private SuspensionResultsMapper suspensionResultsMapper;

    @Test
    public void shouldGetTheSubListOfSuccessfulResults() throws Exception {

        final SuspensionResult suspensionResult_1 = mock(SuspensionResult.class);
        final SuspensionResult suspensionResult_2 = mock(SuspensionResult.class);
        final SuspensionResult suspensionResult_3 = mock(SuspensionResult.class);
        final SuspensionResult suspensionResult_4 = mock(SuspensionResult.class);

        when(suspensionResult_1.getCommandState()).thenReturn(COMMAND_FAILED);
        when(suspensionResult_2.getCommandState()).thenReturn(COMMAND_COMPLETE);
        when(suspensionResult_3.getCommandState()).thenReturn(COMMAND_IN_PROGRESS);
        when(suspensionResult_4.getCommandState()).thenReturn(COMMAND_COMPLETE);

        final List<SuspensionResult> successfulResults = suspensionResultsMapper.getSuccessfulResults(asList(
                suspensionResult_1,
                suspensionResult_2,
                suspensionResult_3,
                suspensionResult_4));

        assertThat(successfulResults.size(), is(2));
        assertThat(successfulResults, hasItem(suspensionResult_2));
        assertThat(successfulResults, hasItem(suspensionResult_4));
    }

    @Test
    public void shouldGetTheSubListOfFailedResults() throws Exception {

        final SuspensionResult suspensionResult_1 = mock(SuspensionResult.class);
        final SuspensionResult suspensionResult_2 = mock(SuspensionResult.class);
        final SuspensionResult suspensionResult_3 = mock(SuspensionResult.class);
        final SuspensionResult suspensionResult_4 = mock(SuspensionResult.class);

        when(suspensionResult_1.getCommandState()).thenReturn(COMMAND_FAILED);
        when(suspensionResult_2.getCommandState()).thenReturn(COMMAND_COMPLETE);
        when(suspensionResult_3.getCommandState()).thenReturn(COMMAND_FAILED);
        when(suspensionResult_4.getCommandState()).thenReturn(COMMAND_IN_PROGRESS);

        final List<SuspensionResult> failedResults = suspensionResultsMapper.getFailedResults(asList(
                suspensionResult_1,
                suspensionResult_2,
                suspensionResult_3,
                suspensionResult_4));

        assertThat(failedResults.size(), is(2));
        assertThat(failedResults, hasItem(suspensionResult_1));
        assertThat(failedResults, hasItem(suspensionResult_3));
    }

    @Test
    public void shouldGetAListOfShutteringExecutorNames() throws Exception {

        final SuspensionResult suspensionResult_1 = mock(SuspensionResult.class);
        final SuspensionResult suspensionResult_2 = mock(SuspensionResult.class);
        final SuspensionResult suspensionResult_3 = mock(SuspensionResult.class);
        final SuspensionResult suspensionResult_4 = mock(SuspensionResult.class);

        when(suspensionResult_1.getSuspendableName()).thenReturn("Executor name 1");
        when(suspensionResult_2.getSuspendableName()).thenReturn("Executor name 2");
        when(suspensionResult_3.getSuspendableName()).thenReturn("Executor name 3");
        when(suspensionResult_4.getSuspendableName()).thenReturn("Executor name 4");

        final List<String> shutteringExecutorNames = suspensionResultsMapper.getSuspendablesNames(asList(
                suspensionResult_1,
                suspensionResult_2,
                suspensionResult_3,
                suspensionResult_4));

        assertThat(shutteringExecutorNames.size(), is(4));
        assertThat(shutteringExecutorNames, hasItem("Executor name 1"));
        assertThat(shutteringExecutorNames, hasItem("Executor name 2"));
        assertThat(shutteringExecutorNames, hasItem("Executor name 3"));
        assertThat(shutteringExecutorNames, hasItem("Executor name 4"));
    }
}
