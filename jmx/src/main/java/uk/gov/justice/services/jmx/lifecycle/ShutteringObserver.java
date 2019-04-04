package uk.gov.justice.services.jmx.lifecycle;

import static java.lang.String.format;

import uk.gov.justice.services.core.lifecycle.events.shuttering.ShutteringRequestedEvent;
import uk.gov.justice.services.core.lifecycle.events.shuttering.UnshutteringRequestedEvent;

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
        logger.info(format("Shuttering requested started at: %s", shutteringRequestedEvent.getShutteringRequestedAt()));
        shutteringFlagProducerBean.setDoShuttering(true);
    }

    public void onUnShutterringRequested(@Observes final UnshutteringRequestedEvent unshutteringRequestedEvent){
        logger.info(format("Unshuttering requested started at: %s", unshutteringRequestedEvent.getUnshutteringRequestedAt()));
        shutteringFlagProducerBean.setDoShuttering(false);
    }

}
