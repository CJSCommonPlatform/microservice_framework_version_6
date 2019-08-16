package uk.gov.justice.services.jmx.command;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.jmx.api.state.ApplicationManagementState.SHUTTERING_IN_PROGRESS;
import static uk.gov.justice.services.jmx.api.state.ApplicationManagementState.UNSHUTTERED;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ApplicationManagementStateRegistryTest {


    @InjectMocks
    private ApplicationManagementStateRegistry applicationManagementStateRegistry;

    @Test
    public void shouldBeUnshutteredByDefault() throws Exception {
        assertThat(applicationManagementStateRegistry.getApplicationManagementState(), is(UNSHUTTERED));
    }

    @Test
    public void shouldSetTheShutteredState() throws Exception {

        assertThat(applicationManagementStateRegistry.getApplicationManagementState(), is(UNSHUTTERED));

        applicationManagementStateRegistry.setApplicationManagementState(SHUTTERING_IN_PROGRESS);
        assertThat(applicationManagementStateRegistry.getApplicationManagementState(), is(SHUTTERING_IN_PROGRESS));
    }
}
