package uk.gov.justice.services.jmx;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.ApplicationStateController;
import uk.gov.justice.services.core.lifecycle.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.core.lifecycle.shuttering.events.UnshutteringRequestedEvent;

public class Shuttering implements ShutteringMBean {

    private ApplicationStateController applicationStateController;

    private UtcClock utcClock;

    public Shuttering(){
        applicationStateController = new ApplicationStateController();
        utcClock = new UtcClock();
    }
    public void doShutteringRequested() {

        final ShutteringRequestedEvent shutteringRequestedEvent = new ShutteringRequestedEvent(this, utcClock.now());
        applicationStateController.fireShutteringRequested(shutteringRequestedEvent);
    }

    public void doUnshutteringRequested() {
        final UnshutteringRequestedEvent unshutteringRequestedEvent = new UnshutteringRequestedEvent(this, utcClock.now());
        applicationStateController.fireUnshutteringRequested(unshutteringRequestedEvent);
    }
}
