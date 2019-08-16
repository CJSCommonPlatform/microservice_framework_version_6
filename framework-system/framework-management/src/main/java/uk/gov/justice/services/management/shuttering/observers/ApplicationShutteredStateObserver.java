package uk.gov.justice.services.management.shuttering.observers;

import static uk.gov.justice.services.jmx.api.state.ApplicationManagementState.SHUTTERED;
import static uk.gov.justice.services.jmx.api.state.ApplicationManagementState.SHUTTERING_IN_PROGRESS;
import static uk.gov.justice.services.jmx.api.state.ApplicationManagementState.UNSHUTTERED;
import static uk.gov.justice.services.jmx.api.state.ApplicationManagementState.UNSHUTTERING_IN_PROGRESS;

import uk.gov.justice.services.jmx.command.ApplicationManagementStateRegistry;
import uk.gov.justice.services.management.shuttering.events.ShutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringCompleteEvent;
import uk.gov.justice.services.management.shuttering.events.UnshutteringRequestedEvent;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

public class ApplicationShutteredStateObserver {

    @Inject
    private ApplicationManagementStateRegistry applicationManagementStateRegistry;

    public void onShutteringRequested(@SuppressWarnings("unused") @Observes final ShutteringRequestedEvent shutteringRequestedEvent) {
        applicationManagementStateRegistry.setApplicationManagementState(SHUTTERING_IN_PROGRESS);
    }

    public void onShutteringComplete(@SuppressWarnings("unused") @Observes final ShutteringCompleteEvent shutteringCompleteEvent) {
        applicationManagementStateRegistry.setApplicationManagementState(SHUTTERED);
    }

    public void onUnshutteringRequested(@SuppressWarnings("unused") @Observes final UnshutteringRequestedEvent unshutteringRequestedEvent) {
        applicationManagementStateRegistry.setApplicationManagementState(UNSHUTTERING_IN_PROGRESS);
    }

    public void onUnshutteringComplete(@SuppressWarnings("unused") @Observes final UnshutteringCompleteEvent unshutteringCompleteEvent) {
        applicationManagementStateRegistry.setApplicationManagementState(UNSHUTTERED);
    }
}
