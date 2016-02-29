package uk.gov.justice.services.core.sender;

import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.services.core.annotation.Component;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ComponentDestinationTest {

    private ComponentDestination componentDestination;

    @Before
    public void setup() {
        componentDestination = new ComponentDestination();
    }

    @Test
    public void shouldReturnCommandController() throws Exception {
        assertThat(componentDestination.getDefault(Component.COMMAND_API), equalTo(Component.COMMAND_CONTROLLER));
    }

    @Test
    public void shouldReturnCommandHandler() throws Exception {
        assertThat(componentDestination.getDefault(Component.COMMAND_CONTROLLER), equalTo(Component.COMMAND_HANDLER));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnComponentWithNoDefaultDestination() throws Exception {
        componentDestination.getDefault(Component.COMMAND_HANDLER);
    }
}