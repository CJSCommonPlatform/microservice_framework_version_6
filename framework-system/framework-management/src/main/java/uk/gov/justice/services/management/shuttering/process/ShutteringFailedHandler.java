package uk.gov.justice.services.management.shuttering.process;

import static java.lang.String.format;
import static uk.gov.justice.services.management.shuttering.api.ShutteringResult.shutteringFailed;

import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.management.shuttering.api.ShutteringExecutor;
import uk.gov.justice.services.management.shuttering.api.ShutteringResult;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

public class ShutteringFailedHandler {

    @Inject
    private Logger logger;

    public ShutteringResult onShutteringFailed(
            final UUID commandId,
            final SystemCommand systemCommand,
            final ShutteringExecutor shutteringExecutor,
            final Throwable exception) {

        final String message = format(
                "%s failed for %s. %s: %s",
                systemCommand.getName(),
                shutteringExecutor.getName(),
                exception.getClass().getName(),
                exception.getMessage());

        logger.error(message, exception);

        return shutteringFailed(
                shutteringExecutor.getName(),
                commandId,
                message,
                systemCommand,
                exception
        );
    }
}
