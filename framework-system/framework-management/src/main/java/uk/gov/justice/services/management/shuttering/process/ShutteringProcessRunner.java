package uk.gov.justice.services.management.shuttering.process;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.ApplicationShutteringCommand;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;
import uk.gov.justice.services.management.shuttering.api.ShutteringResult;

import java.util.List;
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

public class ShutteringProcessRunner {

    @Inject
    private ShutteringExecutorsRunner shutteringExecutorsRunner;

    @Inject
    private ShutteringResultsMapper shutteringResultsMapper;

    @Inject
    private ShutteringPostProcess shutteringPostProcess;

    @Inject
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    public void runShuttering(final UUID commandId, final ApplicationShutteringCommand applicationShutteringCommand) {

        final String systemCommandName = applicationShutteringCommand.getName();

        logger.info(format("Running %s", systemCommandName));

        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                applicationShutteringCommand,
                COMMAND_IN_PROGRESS,
                clock.now(),
                format("%s started", systemCommandName)
        ));

        final List<ShutteringResult> results = shutteringExecutorsRunner.findAndRunShutteringExecutors(
                commandId,
                applicationShutteringCommand
        );

        final List<ShutteringResult> failureResults = shutteringResultsMapper.getFailedResults(results);
        final List<ShutteringResult> successfulResults = shutteringResultsMapper.getSuccessfulResults(results);

        logger.info(format(
                "%s ran with %s success(es) and %s error(s)",
                systemCommandName,
                successfulResults.size(),
                failureResults.size()));

        if (failureResults.isEmpty()) {
            shutteringPostProcess.completeShutteringSuccessfully(successfulResults, commandId, applicationShutteringCommand);
        } else {
            shutteringPostProcess.completeShutteringWithFailures(failureResults, commandId, applicationShutteringCommand);
        }
    }
}
