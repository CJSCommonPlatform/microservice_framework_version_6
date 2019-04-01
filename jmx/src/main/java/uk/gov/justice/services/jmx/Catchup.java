package uk.gov.justice.services.jmx;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.events.catchup.CatchupRequestedEvent;

import javax.enterprise.event.Event;
import javax.inject.Inject;

public class Catchup implements CatchupMBean {

    @Inject
    private UtcClock utcClock;

    @Inject
    private Event<CatchupRequestedEvent> catchupRequestedEventFirer;

    @Override
    public void doCatchupRequested() {
        catchupRequestedEventFirer.fire(new CatchupRequestedEvent(getClass().getSimpleName(), utcClock.now()));
    }
}
