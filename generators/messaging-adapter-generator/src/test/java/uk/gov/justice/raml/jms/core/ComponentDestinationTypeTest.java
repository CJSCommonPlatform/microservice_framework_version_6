package uk.gov.justice.raml.jms.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_API;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;

import javax.jms.Queue;
import javax.jms.Topic;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ComponentDestinationTypeTest {

    private static final String UNKNOWN = "unknown";
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ComponentDestinationType componentDestinationType;

    @Before
    public void setup() {
        componentDestinationType = new ComponentDestinationType();
    }

    @Test
    public void shouldReturnDestinationType() throws Exception {
        assertThat(componentDestinationType.inputTypeFor(COMMAND_API), equalTo(Queue.class));
        assertThat(componentDestinationType.inputTypeFor(COMMAND_CONTROLLER), equalTo(Queue.class));
        assertThat(componentDestinationType.inputTypeFor(COMMAND_HANDLER), equalTo(Queue.class));
        assertThat(componentDestinationType.inputTypeFor(EVENT_PROCESSOR), equalTo(Topic.class));
        assertThat(componentDestinationType.inputTypeFor(EVENT_LISTENER), equalTo(Topic.class));
    }

    @Test
    public void shouldReturnTrueForSupportedComponents() throws Exception {
        assertThat(componentDestinationType.isSupported(COMMAND_API), equalTo(true));
        assertThat(componentDestinationType.isSupported(COMMAND_CONTROLLER), equalTo(true));
        assertThat(componentDestinationType.isSupported(COMMAND_HANDLER), equalTo(true));
        assertThat(componentDestinationType.isSupported(EVENT_PROCESSOR), equalTo(true));
        assertThat(componentDestinationType.isSupported(EVENT_LISTENER), equalTo(true));
    }

    @Test
    public void shouldReturnFalseForUnsupportedComponents() throws Exception {
        assertThat(componentDestinationType.isSupported(EVENT_API), equalTo(false));
        assertThat(componentDestinationType.isSupported(QUERY_API), equalTo(false));
        assertThat(componentDestinationType.isSupported(QUERY_CONTROLLER), equalTo(false));
        assertThat(componentDestinationType.isSupported(QUERY_VIEW), equalTo(false));
        assertThat(componentDestinationType.isSupported(UNKNOWN), equalTo(false));
    }

    @Test
    public void shouldThrowExceptionIfNoInputDestinationTypeFound() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("No input destination type defined for service component of type EVENT_API");

        componentDestinationType.inputTypeFor(EVENT_API);
    }
}