package uk.gov.justice.services.core.annotation;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import javax.jms.Queue;
import javax.jms.Topic;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ComponentTest {

    private static final String PILLAR_COMMAND = "command";
    private static final String PILLAR_QUERY = "query";
    private static final String PILLAR_EVENT = "event";

    private static final String TIER_API = "api";
    private static final String TIER_CONTROLLER = "controller";
    private static final String TIER_HANDLER = "handler";
    private static final String TIER_LISTENER = "listener";
    private static final String TIER_PROCESSOR = "processor";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldReturnPillar() throws Exception {
        assertThat(Component.COMMAND_API.pillar(), equalTo(PILLAR_COMMAND));
        assertThat(Component.QUERY_API.pillar(), equalTo(PILLAR_QUERY));
        assertThat(Component.COMMAND_CONTROLLER.pillar(), equalTo(PILLAR_COMMAND));
        assertThat(Component.COMMAND_HANDLER.pillar(), equalTo(PILLAR_COMMAND));
        assertThat(Component.EVENT_LISTENER.pillar(), equalTo(PILLAR_EVENT));
        assertThat(Component.EVENT_PROCESSOR.pillar(), equalTo(PILLAR_EVENT));
    }

    @Test
    public void shouldReturnTier() throws Exception {
        assertThat(Component.COMMAND_API.tier(), equalTo(TIER_API));
        assertThat(Component.QUERY_API.tier(), equalTo(TIER_API));
        assertThat(Component.COMMAND_CONTROLLER.tier(), equalTo(TIER_CONTROLLER));
        assertThat(Component.COMMAND_HANDLER.tier(), equalTo(TIER_HANDLER));
        assertThat(Component.EVENT_LISTENER.tier(), equalTo(TIER_LISTENER));
        assertThat(Component.EVENT_PROCESSOR.tier(), equalTo(TIER_PROCESSOR));
    }

    @Test
    public void shouldReturnDestinationType() throws Exception {
        assertThat(Component.COMMAND_API.destinationType(), equalTo(Queue.class));
        assertThat(Component.COMMAND_CONTROLLER.destinationType(), equalTo(Queue.class));
        assertThat(Component.COMMAND_HANDLER.destinationType(), equalTo(Queue.class));
        assertThat(Component.EVENT_LISTENER.destinationType(), equalTo(Topic.class));
    }

    @Test
    public void shouldConstructComponentByTierAndPillar() {
        assertThat(Component.valueOf("command", "api"), is(Component.COMMAND_API));
        assertThat(Component.valueOf("command", "controller"), is(Component.COMMAND_CONTROLLER));
        assertThat(Component.valueOf("command", "handler"), is(Component.COMMAND_HANDLER));
        assertThat(Component.valueOf("event", "listener"), is(Component.EVENT_LISTENER));
        assertThat(Component.valueOf("event", "processor"), is(Component.EVENT_PROCESSOR));
        assertThat(Component.valueOf("query", "api"), is(Component.QUERY_API));
    }

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

    @Test
    public void shouldReturnTrueIfContainsGivenString() {

        assertTrue(Component.contains("COMMAND_API"));
        assertTrue(Component.contains("COMMAND_CONTROLLER"));
        assertTrue(Component.contains("COMMAND_HANDLER"));
        assertTrue(Component.contains("EVENT_LISTENER"));
        assertTrue(Component.contains("EVENT_PROCESSOR"));
        assertTrue(Component.contains("QUERY_API"));
        assertTrue(Component.contains("QUERY_CONTROLLER"));
        assertTrue(Component.contains("QUERY_VIEW"));
    }

    @Test
    public void shouldReturnFalseIfDoesNotContainGivenString() {
        assertFalse(Component.contains("COMMAND_API_aaa"));
        assertFalse(Component.contains("UNKNOWN"));
    }

    @Test
    public void shouldReturnStringContainingSeparatedNames() {
        assertThat(Component.names(", "), is("COMMAND_API, COMMAND_CONTROLLER, COMMAND_HANDLER, EVENT_LISTENER, EVENT_PROCESSOR, QUERY_API, QUERY_CONTROLLER, QUERY_VIEW"));
    }

}