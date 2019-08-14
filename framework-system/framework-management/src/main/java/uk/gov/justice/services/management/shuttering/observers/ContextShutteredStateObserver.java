package uk.gov.justice.services.management.shuttering.observers;

import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.CONTEXT_SHUTTERED;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.CONTEXT_UNSHUTTERED;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.SHUTTERING_STARTED;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.UNSHUTTERING_STARTED;

import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState;

import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.event.Observes;
import javax.faces.bean.ApplicationScoped;

@ApplicationScoped
public class ContextShutteredStateObserver {

    private final AtomicReference<ContextShutteredState> shutteredState = new AtomicReference<>(CONTEXT_UNSHUTTERED);

    public void onShutteringRequested(@SuppressWarnings("unused") @Observes final ShutteringRequestedEvent shutteringRequestedEvent) {
        shutteredState.set(SHUTTERING_STARTED);
    }

    public void onShutteringComplete(@SuppressWarnings("unused") @Observes final ShutteringCompleteEvent shutteringCompleteEvent) {
        shutteredState.set(CONTEXT_SHUTTERED);
    }

    public void onUnshutteringRequested(@SuppressWarnings("unused") @Observes final UnshutteringRequestedEvent unshutteringRequestedEvent) {
        shutteredState.set(UNSHUTTERING_STARTED);
    }

    public void onUnshutteringComplete(@SuppressWarnings("unused") @Observes final UnshutteringCompleteEvent unshutteringCompleteEvent) {
        shutteredState.set(CONTEXT_UNSHUTTERED);
    }

    public ContextShutteredState getShutteredState() {
        return shutteredState.get();
    }
}
