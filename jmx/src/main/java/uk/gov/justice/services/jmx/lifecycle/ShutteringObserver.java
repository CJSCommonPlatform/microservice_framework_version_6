package uk.gov.justice.services.jmx.lifecycle;

import uk.gov.justice.services.core.lifecycle.events.shuttering.ShutteringCompleteEvent;
import uk.gov.justice.services.core.lifecycle.events.shuttering.ShutteringRequestedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
public class ShutteringObserver {

    @Inject
    private Logger logger;

    @Inject
    private ShutteringFlagProducerBean shutteringFlagProducerBean;


    public void onShutterringRequested(@Observes final ShutteringRequestedEvent shutteringRequestedEvent){
        logger.info("Shuttering requested started at: " + shutteringRequestedEvent.getShutteringRequestedAt());
        shutteringFlagProducerBean.setDoShuttering(true);
    }

    public void onUnShutterringRequested(@Observes final ShutteringCompleteEvent shutteringCompleteEvent){
        logger.info("Unshuttering requested started at: " + shutteringCompleteEvent.getShutteringCompleteAt());
        shutteringFlagProducerBean.setDoShuttering(false);
    }

}
