package uk.gov.justice.services.management.shuttering.process;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.management.shuttering.api.ShutteringExecutor;
import uk.gov.justice.services.management.shuttering.api.ShutteringResult;
import uk.gov.justice.services.management.shuttering.commands.ApplicationShutteringCommand;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

public class ShutterRunner {

    @Inject
    private ShutteringExecutorProvider shutteringExecutorProvider;

    @Inject
    private ShutteringFailedHandler shutteringFailedHandler;

    @Inject
    private Logger logger;

    public List<ShutteringResult> runShuttering(final UUID commandId, final ApplicationShutteringCommand applicationShutteringCommand) {
        return shutteringExecutorProvider.getShutteringExecutors().stream()
                .filter(ShutteringExecutor::shouldShutter)
                .map(shutteringExecutor -> shutter(commandId, applicationShutteringCommand, shutteringExecutor))
                .collect(toList());
    }

    private ShutteringResult shutter(final UUID commandId, final ApplicationShutteringCommand applicationShutteringCommand, final ShutteringExecutor shutteringExecutor) {

        logger.info(format("Shuttering %s", shutteringExecutor.getName()));

        try {
            return shutteringExecutor.shutter(commandId, applicationShutteringCommand);
        } catch (final Throwable e) {
            return shutteringFailedHandler.onShutteringFailed(commandId, applicationShutteringCommand, shutteringExecutor, e);
        }
    }
}
