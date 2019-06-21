package uk.gov.justice.services.management.shuttering.observers.shuttering;

import uk.gov.justice.services.management.shuttering.events.ShutteringProcessStartedEvent;
import uk.gov.justice.services.management.shuttering.process.CommandApiShutteringBean;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

public class ShutterCommandApiObserver {

    @Inject
    private CommandApiShutteringBean commandApiShutteringBean;

    @Inject
    private ShutteringRegistry shutteringRegistry;

    @Inject
    private Logger logger;

    @PostConstruct
    public void registerAsShutterable() {
        shutteringRegistry.registerAsShutterable(getClass());
    }

    public void onShutteringProcessStarted(@Observes final ShutteringProcessStartedEvent shutteringProcessStartedEvent) {

        logger.info("Shuttering Command API");

        commandApiShutteringBean.shutter(shutteringProcessStartedEvent.getTarget());

        logger.info("Shuttering of Command API complete");

        shutteringRegistry.markShutteringCompleteFor(getClass(), shutteringProcessStartedEvent.getTarget());
    }
}
