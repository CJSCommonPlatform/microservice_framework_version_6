package uk.gov.justice.services.management.suspension.handler;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRED;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.logging.MdcLoggerInterceptor;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;
import uk.gov.justice.services.management.suspension.api.SuspensionResult;
import uk.gov.justice.services.management.suspension.commands.SuspensionCommand;
import uk.gov.justice.services.management.suspension.process.SuspendablesRunner;
import uk.gov.justice.services.management.suspension.process.SuspensionPostProcess;
import uk.gov.justice.services.management.suspension.process.SuspensionResultsMapper;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.transaction.Transactional;

import org.slf4j.Logger;

@Stateless
public class SuspensionBean {

    @Inject
    private SuspendablesRunner suspendablesRunner;

    @Inject
    private SuspensionResultsMapper suspensionResultsMapper;

    @Inject
    private SuspensionPostProcess suspensionPostProcess;

    @Inject
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    @Interceptors(MdcLoggerInterceptor.class)
    @Transactional(REQUIRED)
    public void runSuspension(final UUID commandId, final SuspensionCommand suspensionCommand) {
        final String systemCommandName = suspensionCommand.getName();

        logger.info(format("Running %s", systemCommandName));

        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                suspensionCommand,
                COMMAND_IN_PROGRESS,
                clock.now(),
                format("%s started", systemCommandName)
        ));

        final List<SuspensionResult> results = suspendablesRunner.findAndRunSuspendables(
                commandId,
                suspensionCommand
        );

        final List<SuspensionResult> failureResults = suspensionResultsMapper.getFailedResults(results);
        final List<SuspensionResult> successfulResults = suspensionResultsMapper.getSuccessfulResults(results);

        logger.info(format(
                "%s ran with %s success(es) and %s error(s)",
                systemCommandName,
                successfulResults.size(),
                failureResults.size()));

        if (failureResults.isEmpty()) {
            suspensionPostProcess.completeSuspensionSuccessfully(successfulResults, commandId, suspensionCommand);
        } else {
            suspensionPostProcess.completeSuspensionWithFailures(failureResults, commandId, suspensionCommand);
        }
    }
}
