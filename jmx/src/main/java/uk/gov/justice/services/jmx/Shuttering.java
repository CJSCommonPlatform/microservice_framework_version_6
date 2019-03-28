package uk.gov.justice.services.jmx;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.events.shuttering.ShutteringRequestedEvent;
import uk.gov.justice.services.core.lifecycle.events.shuttering.UnshutteringRequestedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@ApplicationScoped
public class Shuttering implements ShutteringMBean {

    @Inject
    private UtcClock clock;

    @Inject
    private Event<ShutteringRequestedEvent> shutteringRequestedEventFirer;

    @Inject
    private Event<UnshutteringRequestedEvent> unshutteringRequestedEventFirer;

    public void doShutteringRequested() {
        shutteringRequestedEventFirer.fire(new ShutteringRequestedEvent(this, clock.now()));
    }

    public void doUnshutteringRequested() {
        unshutteringRequestedEventFirer.fire(new UnshutteringRequestedEvent(this, clock.now()));
    }
}
