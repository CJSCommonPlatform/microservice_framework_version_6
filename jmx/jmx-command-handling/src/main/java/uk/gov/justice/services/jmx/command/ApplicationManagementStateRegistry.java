package uk.gov.justice.services.jmx.command;

import static uk.gov.justice.services.jmx.api.state.ApplicationManagementState.UNSHUTTERED;

import uk.gov.justice.services.jmx.api.state.ApplicationManagementState;

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Singleton;

@Singleton
public class ApplicationManagementStateRegistry {

    private final AtomicReference<ApplicationManagementState> applicationState = new AtomicReference<>(UNSHUTTERED);

    public void setApplicationManagementState(final ApplicationManagementState applicationManagementState) {
        applicationState.set(applicationManagementState);
    }

    public ApplicationManagementState getApplicationManagementState() {
        return applicationState.get();
    }
}
