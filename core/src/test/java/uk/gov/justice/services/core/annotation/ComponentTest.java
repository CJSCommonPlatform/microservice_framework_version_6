package uk.gov.justice.services.core.annotation;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ComponentTest {

    private static final String PILLAR_COMMANDS = "commands";
    private static final String TIER_API = "api";
    private static final String TIER_CONTROLLER = "controller";
    private static final String TIER_HANDLER = "handler";

    @Test
    public void shouldReturnPillar() throws Exception {
        assertThat(Component.COMMAND_API.pillar(), equalTo(PILLAR_COMMANDS));
        assertThat(Component.COMMAND_CONTROLLER.pillar(), equalTo(PILLAR_COMMANDS));
        assertThat(Component.COMMAND_HANDLER.pillar(), equalTo(PILLAR_COMMANDS));
    }

    @Test
    public void shouldReturnTier() throws Exception {
        assertThat(Component.COMMAND_API.tier(), equalTo(TIER_API));
        assertThat(Component.COMMAND_CONTROLLER.tier(), equalTo(TIER_CONTROLLER));
        assertThat(Component.COMMAND_HANDLER.tier(), equalTo(TIER_HANDLER));
    }

}