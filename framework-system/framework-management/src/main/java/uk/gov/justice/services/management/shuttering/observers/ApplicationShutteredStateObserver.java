package uk.gov.justice.services.management.shuttering.observers;

import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.SHUTTERED;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.SHUTTERING_IN_PROGRESS;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.UNSHUTTERED;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.UNSHUTTERING_IN_PROGRESS;

import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

public class ApplicationShutteredStateObserver {

    @Inject
    private ShutteringStateRegistry shutteringStateRegistry;

    public void onShutteringRequested(@SuppressWarnings("unused") @Observes final ShutteringRequestedEvent shutteringRequestedEvent) {
        shutteringStateRegistry.setShutteredState(SHUTTERING_IN_PROGRESS);
    }

    public void onShutteringComplete(@SuppressWarnings("unused") @Observes final ShutteringCompleteEvent shutteringCompleteEvent) {
        shutteringStateRegistry.setShutteredState(SHUTTERED);
    }

    public void onUnshutteringRequested(@SuppressWarnings("unused") @Observes final UnshutteringRequestedEvent unshutteringRequestedEvent) {
        shutteringStateRegistry.setShutteredState(UNSHUTTERING_IN_PROGRESS);
    }

    public void onUnshutteringComplete(@SuppressWarnings("unused") @Observes final UnshutteringCompleteEvent unshutteringCompleteEvent) {
        shutteringStateRegistry.setShutteredState(UNSHUTTERED);
    }
}
