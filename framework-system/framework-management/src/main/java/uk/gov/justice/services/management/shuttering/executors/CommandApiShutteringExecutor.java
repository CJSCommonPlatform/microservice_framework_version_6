package uk.gov.justice.services.management.shuttering.executors;

import static uk.gov.justice.services.management.shuttering.api.ShutteringResult.shutteringSucceeded;

import uk.gov.justice.services.jmx.api.command.ApplicationShutteringCommand;
import uk.gov.justice.services.management.shuttering.api.ShutteringExecutor;
import uk.gov.justice.services.management.shuttering.api.ShutteringResult;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

public class CommandApiShutteringExecutor implements ShutteringExecutor {

    @Inject
    private CommandApiShutteringBean commandApiShutteringBean;

    @Inject
    private Logger logger;

    @Override
    public boolean shouldShutter() {
        return true;
    }

    @Override
    public boolean shouldUnshutter() {
        return true;
    }

    @Override
    public ShutteringResult shutter(final UUID commandId, final ApplicationShutteringCommand applicationShutteringCommand) {

        logger.info("Shuttering Command API");

        commandApiShutteringBean.shutter();

        logger.info("Shuttering of Command API complete");

        return shutteringSucceeded(
                getName(),
                commandId,
                "Command API shuttered with no errors",
                applicationShutteringCommand
        );
    }

    @Override
    public ShutteringResult unshutter(final UUID commandId, final ApplicationShutteringCommand applicationShutteringCommand) {

        logger.info("Unshuttering Command API");

        commandApiShutteringBean.unshutter();

        logger.info("Unshuttering of Command API complete");

        return shutteringSucceeded(
                getName(),
                commandId,
                "Command API unshuttered with no errors",
                applicationShutteringCommand
        );
    }
}
