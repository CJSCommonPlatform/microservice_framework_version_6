package uk.gov.justice.services.core.jmx;


import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.ApplicationStateController;
import uk.gov.justice.services.core.lifecycle.catchup.events.CatchupRequestedEvent;
import uk.gov.justice.services.jmx.CatchupMBean;

public class Catchup implements CatchupMBean {

    private ApplicationStateController applicationStateController;

    private UtcClock utcClock;

    public Catchup() {
        applicationStateController = new ApplicationStateController();
        utcClock = new UtcClock();
    }

    @Override
    public void doCatchupRequested() {
        final CatchupRequestedEvent catchupRequestedEvent = new CatchupRequestedEvent(this, utcClock.now());
        applicationStateController.fireCatchupRequested(catchupRequestedEvent);
    }
}
