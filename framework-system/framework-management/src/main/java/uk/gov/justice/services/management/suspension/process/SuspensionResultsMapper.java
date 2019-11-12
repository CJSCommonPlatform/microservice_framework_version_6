package uk.gov.justice.services.management.suspension.process;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import uk.gov.justice.services.management.suspension.api.SuspensionResult;

import java.util.List;

public class SuspensionResultsMapper {

    public List<SuspensionResult> getSuccessfulResults(final List<SuspensionResult> suspensionResults) {

        return suspensionResults.stream()
                .filter(shutteringResult -> shutteringResult.getCommandState() == COMMAND_COMPLETE)
                .collect(toList());
    }

    public List<SuspensionResult> getFailedResults(final List<SuspensionResult> suspensionResults) {

        return suspensionResults.stream()
                .filter(shutteringResult -> shutteringResult.getCommandState() == COMMAND_FAILED)
                .collect(toList());
    }

    public List<String> getSuspendablesNames(final List<SuspensionResult> suspensionResults) {
        return suspensionResults.stream()
                .map(SuspensionResult::getSuspendableName)
                .collect(toList());
    }
}
