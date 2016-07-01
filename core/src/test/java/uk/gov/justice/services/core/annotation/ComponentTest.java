package uk.gov.justice.services.core.annotation;


import static org.hamcrest.CoreMatchers.equalTo;
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
import static uk.gov.justice.services.core.annotation.Component.componentFrom;
import static uk.gov.justice.services.core.annotation.Component.contains;
import static uk.gov.justice.services.core.annotation.Component.names;
import static uk.gov.justice.services.core.annotation.Component.valueOf;

import uk.gov.justice.services.core.util.TestInjectionPoint;

import javax.inject.Inject;
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
    private static final String FIELD_NAME = "field";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldReturnPillar() throws Exception {
        assertThat(COMMAND_API.pillar(), equalTo(PILLAR_COMMAND));
        assertThat(QUERY_API.pillar(), equalTo(PILLAR_QUERY));
        assertThat(COMMAND_CONTROLLER.pillar(), equalTo(PILLAR_COMMAND));
        assertThat(COMMAND_HANDLER.pillar(), equalTo(PILLAR_COMMAND));
        assertThat(EVENT_LISTENER.pillar(), equalTo(PILLAR_EVENT));
        assertThat(EVENT_PROCESSOR.pillar(), equalTo(PILLAR_EVENT));
        assertThat(EVENT_API.pillar(), equalTo(PILLAR_EVENT));
    }

    @Test
    public void shouldReturnTier() throws Exception {
        assertThat(COMMAND_API.tier(), equalTo(TIER_API));
        assertThat(QUERY_API.tier(), equalTo(TIER_API));
        assertThat(COMMAND_CONTROLLER.tier(), equalTo(TIER_CONTROLLER));
        assertThat(COMMAND_HANDLER.tier(), equalTo(TIER_HANDLER));
        assertThat(EVENT_LISTENER.tier(), equalTo(TIER_LISTENER));
        assertThat(EVENT_PROCESSOR.tier(), equalTo(TIER_PROCESSOR));
        assertThat(EVENT_API.tier(), equalTo(TIER_API));
    }

    @Test
    public void shouldReturnDestinationType() throws Exception {
        assertThat(COMMAND_API.inputDestinationType(), equalTo(Queue.class));
        assertThat(COMMAND_CONTROLLER.inputDestinationType(), equalTo(Queue.class));
        assertThat(COMMAND_HANDLER.inputDestinationType(), equalTo(Queue.class));
        assertThat(EVENT_LISTENER.inputDestinationType(), equalTo(Topic.class));
        assertThat(EVENT_API.inputDestinationType(), equalTo(null));
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
        exception.expectMessage("No enum constant for pillar: invalidPillar, tier: api");

        valueOf("invalidPillar", "api");

    }

    @Test
    public void shouldThrowExceptionIfTierInvalid() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("No enum constant for pillar: commands, tier: invalidTier");

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

    @Test
    public void shouldReturnStringContainingSeparatedNames() {
        assertThat(names(", "), is("COMMAND_API, COMMAND_CONTROLLER, COMMAND_HANDLER, EVENT_LISTENER, EVENT_PROCESSOR, EVENT_API, QUERY_API, QUERY_CONTROLLER, QUERY_VIEW"));
    }

    @Test
    public void shouldReturnFieldLevelComponent() throws NoSuchFieldException {
        assertThat(componentFrom(new TestInjectionPoint(FieldLevelAnnotation.class.getDeclaredField(FIELD_NAME))), equalTo(COMMAND_CONTROLLER));
    }

    @Test
    public void shouldReturnClassLevelComponent() throws NoSuchFieldException {
        assertThat(componentFrom(new TestInjectionPoint(ClassLevelAnnotation.class.getDeclaredField(FIELD_NAME))), equalTo(COMMAND_HANDLER));
    }

    @Test
    public void shouldReturnClassLevelAdaptorComponent() throws NoSuchFieldException {
        assertThat(componentFrom(new TestInjectionPoint(AdapterAnnotation.class.getDeclaredField(FIELD_NAME))), equalTo(EVENT_LISTENER));
    }

    @Test
    public void shouldReturnClassLevelComponentForMethodInjectionPoint() throws NoSuchFieldException {
        assertThat(componentFrom(new TestInjectionPoint(MethodAnnotation.class.getDeclaredMethods()[0])), equalTo(QUERY_API));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnMissingComponentAnnotation() throws NoSuchFieldException {
        componentFrom(new TestInjectionPoint(NoAnnotation.class.getDeclaredField(FIELD_NAME)));
    }


    public static class FieldLevelAnnotation {

        @Inject
        @ServiceComponent(COMMAND_CONTROLLER)
        Object field;

    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class ClassLevelAnnotation {

        @Inject
        Object field;

    }

    @Adapter(EVENT_LISTENER)
    public static class AdapterAnnotation {

        @Inject
        Object field;

    }

    public static class NoAnnotation {

        @Inject
        Object field;

    }

    @ServiceComponent(QUERY_API)
    public static class MethodAnnotation {

        @Inject
        public void test(Object field) {

        }

    }


}