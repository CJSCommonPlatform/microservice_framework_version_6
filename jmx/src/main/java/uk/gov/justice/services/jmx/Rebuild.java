package uk.gov.justice.services.jmx;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.events.rebuild.RebuildRequestedEvent;

import javax.enterprise.event.Event;
import javax.inject.Inject;

public class Rebuild implements RebuildMBean {

    @Inject
    private UtcClock clock;

    @Inject
    private Event<RebuildRequestedEvent> rebuildRequestedEventEventFirer;

    @Override
    public void doRebuildRequested() {
        rebuildRequestedEventEventFirer.fire(new RebuildRequestedEvent(getClass().getSimpleName(), clock.now()));
    }
}
