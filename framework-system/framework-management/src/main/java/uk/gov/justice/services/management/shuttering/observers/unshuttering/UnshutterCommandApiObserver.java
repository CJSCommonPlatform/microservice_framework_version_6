package uk.gov.justice.services.management.shuttering.observers.unshuttering;

import uk.gov.justice.services.management.shuttering.events.UnshutteringProcessStartedEvent;
import uk.gov.justice.services.management.shuttering.process.CommandApiShutteringBean;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

public class UnshutterCommandApiObserver {

    @Inject
    private CommandApiShutteringBean commandApiShutteringBean;

    @Inject
    private UnshutteringRegistry shutteringRegistry;

    @Inject
    private Logger logger;

    @PostConstruct
    public void registerAsUnshutterable() {
        shutteringRegistry.registerAsUnshutterable(getClass());
    }

    public void onUnshutteringProcessStarted(@Observes final UnshutteringProcessStartedEvent unshutteringProcessStartedEvent) {

        logger.info("Unshuttering Command API");

        commandApiShutteringBean.unshutter(unshutteringProcessStartedEvent.getTarget());

        logger.info("Unshuttering of Command API complete");

        shutteringRegistry.markUnshutteringCompleteFor(getClass(), unshutteringProcessStartedEvent.getTarget());
    }
}
