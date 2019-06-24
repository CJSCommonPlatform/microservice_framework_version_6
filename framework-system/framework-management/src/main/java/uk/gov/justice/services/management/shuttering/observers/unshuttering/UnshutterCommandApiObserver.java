package uk.gov.justice.services.management.shuttering.observers.unshuttering;

import uk.gov.justice.services.management.shuttering.events.UnshutteringProcessStartedEvent;
import uk.gov.justice.services.management.shuttering.process.CommandApiShutteringBean;
import uk.gov.justice.services.management.shuttering.startup.UnshutteringExecutor;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

@UnshutteringExecutor
public class UnshutterCommandApiObserver {

    @Inject
    private CommandApiShutteringBean commandApiShutteringBean;

    @Inject
    private UnshutteringRegistry unshutteringRegistry;

    @Inject
    private Logger logger;

    public void onUnshutteringProcessStarted(@Observes final UnshutteringProcessStartedEvent unshutteringProcessStartedEvent) {

        logger.info("Unshuttering Command API");

        commandApiShutteringBean.unshutter();

        logger.info("Unshuttering of Command API complete");

        unshutteringRegistry.markUnshutteringCompleteFor(getClass(), unshutteringProcessStartedEvent.getTarget());
    }
}
