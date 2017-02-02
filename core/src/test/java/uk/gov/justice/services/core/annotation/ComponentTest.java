package uk.gov.justice.services.core.annotation;


import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_API;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.annotation.Component.contains;
import static uk.gov.justice.services.core.annotation.Component.valueOf;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ComponentTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(Component.class);
    }

    @Test
    public void shouldConstructComponentByTierAndPillar() {
        assertThat(valueOf("command", "api"), is(COMMAND_API));
        assertThat(valueOf("command", "controller"), is(COMMAND_CONTROLLER));
        assertThat(valueOf("command", "handler"), is(COMMAND_HANDLER));
        assertThat(valueOf("event", "listener"), is(EVENT_LISTENER));
        assertThat(valueOf("event", "processor"), is(EVENT_PROCESSOR));
        assertThat(valueOf("query", "api"), is(QUERY_API));
        assertThat(valueOf("event", "api"), is(EVENT_API));
    }

    @Test
    public void shouldThrowExceptionIfPillarInvalid() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("No component matches pillar: invalidPillar, tier: api");

        valueOf("invalidPillar", "api");

    }

    @Test
    public void shouldThrowExceptionIfTierInvalid() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("No component matches pillar: commands, tier: invalidTier");

        valueOf("commands", "invalidTier");

    }

    @Test
    public void shouldReturnTrueIfContainsGivenString() {

        assertTrue(contains("COMMAND_API"));
        assertTrue(contains("COMMAND_CONTROLLER"));
        assertTrue(contains("COMMAND_HANDLER"));
        assertTrue(contains("EVENT_LISTENER"));
        assertTrue(contains("EVENT_PROCESSOR"));
        assertTrue(contains("QUERY_API"));
        assertTrue(contains("QUERY_CONTROLLER"));
        assertTrue(contains("QUERY_VIEW"));
        assertTrue(contains("EVENT_API"));
    }

    @Test
    public void shouldReturnFalseIfDoesNotContainGivenString() {
        assertFalse(contains("COMMAND_API_aaa"));
        assertFalse(contains("UNKNOWN"));
    }
}