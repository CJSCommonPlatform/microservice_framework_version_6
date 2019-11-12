package uk.gov.justice.services.management.suspension.process;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;
import uk.gov.justice.services.management.suspension.api.SuspensionResult;
import uk.gov.justice.services.management.suspension.commands.SuspensionCommand;

import java.util.List;
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

public class SuspensionPostProcess {

    @Inject
    private SuspensionResultsMapper suspensionResultsMapper;

    @Inject
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    public void completeSuspensionSuccessfully(final List<SuspensionResult> successfulResults, final UUID commandId, final SuspensionCommand suspensionCommand) {

        final String suspensionCommandName = suspensionCommand.getName();

        final List<String> suspendablesNames = suspensionResultsMapper.getSuspendablesNames(successfulResults);

        final String message = format(
                "%s completed successfully for %s",
                suspensionCommandName,
                suspendablesNames);

        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                suspensionCommand,
                COMMAND_COMPLETE,
                clock.now(),
                message
        ));
    }

    public void completeSuspensionWithFailures(final List<SuspensionResult> failureResults, final UUID commandId, final SuspensionCommand suspensionCommand) {

        final String suspensionCommandName = suspensionCommand.getName();

        logger.error(format("%s failed with the following %d errors:", suspensionCommandName, failureResults.size()));

        failureResults.forEach(suspensionResult -> logger.error(format("%s Error: %s", suspensionCommandName, suspensionResult.getMessage())));

        final List<String> suspendablesNames = suspensionResultsMapper
                .getSuspendablesNames(failureResults);

        final String message = format(
                "%s failed. The following Suspendables failed: %s",
                suspensionCommandName,
                suspendablesNames);

        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                suspensionCommand,
                COMMAND_FAILED,
                clock.now(),
                message
        ));
    }
}
