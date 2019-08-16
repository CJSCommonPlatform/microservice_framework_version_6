package uk.gov.justice.services.management.shuttering.observers;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.InjectMocks;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.SHUTTERING_IN_PROGRESS;
import static uk.gov.justice.services.management.shuttering.observers.shuttering.ContextShutteredState.UNSHUTTERED;


@RunWith(MockitoJUnitRunner.class)
public class ShutteringStateRegistryTest {


    @InjectMocks
    private ShutteringStateRegistry shutteringStateRegistry;

    @Test
    public void shouldBeUnshutteredByDefault() throws Exception {
        assertThat(shutteringStateRegistry.getShutteredState(), is(UNSHUTTERED));
    }

    @Test
    public void shouldSetTheShutteredState() throws Exception {

        assertThat(shutteringStateRegistry.getShutteredState(), is(UNSHUTTERED));

        shutteringStateRegistry.setShutteredState(SHUTTERING_IN_PROGRESS);
        assertThat(shutteringStateRegistry.getShutteredState(), is(SHUTTERING_IN_PROGRESS));
    }
}
