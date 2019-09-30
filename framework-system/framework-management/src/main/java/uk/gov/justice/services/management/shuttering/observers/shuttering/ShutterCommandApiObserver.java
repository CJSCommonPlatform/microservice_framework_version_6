package uk.gov.justice.services.management.shuttering.observers.shuttering;

import uk.gov.justice.services.management.shuttering.events.ShutteringProcessStartedEvent;
import uk.gov.justice.services.management.shuttering.process.CommandApiShutteringBean;
import uk.gov.justice.services.management.shuttering.startup.ShutteringExecutor;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

@ShutteringExecutor
public class ShutterCommandApiObserver {

    @Inject
    private CommandApiShutteringBean commandApiShutteringBean;

    @Inject
    private ShutteringRegistry shutteringRegistry;

    @Inject
    private Logger logger;

    public void onShutteringProcessStarted(@Observes final ShutteringProcessStartedEvent shutteringProcessStartedEvent) {

        logger.info("Shuttering Command API");

        commandApiShutteringBean.shutter();

        logger.info("Shuttering of Command API complete");

        shutteringRegistry.markShutteringCompleteFor(
                shutteringProcessStartedEvent.getCommandId(),
                getClass(),
                shutteringProcessStartedEvent.getTarget());
    }
}
