package uk.gov.justice.services.core.annotation;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

    @Test
    public void shouldConstructComponentByTierAndPillar() {
        assertThat(Component.valueOf("commands", "api"), is(Component.COMMAND_API));
        assertThat(Component.valueOf("commands", "controller"), is(Component.COMMAND_CONTROLLER));
        assertThat(Component.valueOf("commands", "handler"), is(Component.COMMAND_HANDLER));
        assertThat(Component.valueOf("events", "listener"), is(Component.EVENT_LISTENER));
    }
    
    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    @Test
    public void shouldThrowExceptionIfPillarInvalid() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("No enum constant for pillar: invalidPillar, tier: api");
        
        Component.valueOf("invalidPillar", "api");
        
    }

    @Test
    public void shouldThrowExceptionIfTierInvalid() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("No enum constant for pillar: commands, tier: invalidTier");
        
        Component.valueOf("commands", "invalidTier");
        
    }

    
}