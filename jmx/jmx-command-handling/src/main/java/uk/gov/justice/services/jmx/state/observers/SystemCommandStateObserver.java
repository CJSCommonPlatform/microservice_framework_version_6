package uk.gov.justice.services.jmx.state.observers;

import uk.gov.justice.services.jmx.logging.MdcLogger;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

public class SystemCommandStateObserver {

    @Inject
    private SystemCommandStateHandler systemCommandStateHandler;

    @Inject
    private MdcLogger mdcLogger;

    public void onSystemCommandStateChanged(@Observes final SystemCommandStateChangedEvent systemCommandStateChangedEvent) {

        mdcLogger
                .mdcLoggerConsumer()
                .accept(() -> systemCommandStateHandler.handleSystemCommandStateChanged(systemCommandStateChangedEvent));
    }
}
