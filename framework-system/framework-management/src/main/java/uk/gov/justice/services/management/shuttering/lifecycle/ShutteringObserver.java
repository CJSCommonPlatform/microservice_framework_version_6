package uk.gov.justice.services.management.shuttering.lifecycle;

import static java.lang.String.format;

import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
public class ShutteringObserver {

    @Inject
    private Logger logger;

    @Inject
    private ShutteringBean shutteringBean;

    public void onShutteringRequested(@Observes final ShutteringRequestedEvent shutteringRequestedEvent) {
        logger.info(format("Shuttering requested started at: %s", shutteringRequestedEvent.getShutteringRequestedAt()));
        shutteringBean.shutter(shutteringRequestedEvent.getTarget());
    }

    public void onShutteringComplete(@Observes final ShutteringCompleteEvent shutteringCompleteEvent) {
        logger.info(format("Shuttering completed at: %s", shutteringCompleteEvent.getShutteringCompleteAt()));
    }

    public void onUnShutteringRequested(@Observes final UnshutteringRequestedEvent unshutteringRequestedEvent) {
        logger.info(format("Unshuttering requested started at: %s", unshutteringRequestedEvent.getUnshutteringRequestedAt()));
        shutteringBean.unshutter(unshutteringRequestedEvent.getTarget());
    }

    public void onUnshutteringComplete(@Observes final UnshutteringCompleteEvent unshutteringCompleteEvent) {
        logger.info(format("Unshuttering completed at: %s", unshutteringCompleteEvent.getUnshutteringCompletedAt()));
    }
}
