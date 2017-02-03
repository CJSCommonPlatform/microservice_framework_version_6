package uk.gov.justice.services.test.utils.core.matchers;

import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodActionMatcher.hasMethodThatHandlesAction;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.Test;

/**
 * Unit tests for the {@link HandlerMethodActionMatcher} class.
 */
public class HandlerMethodActionMatcherTest {

    @Test
    public void shouldMatchMethodWithHandlesAnnotation() throws Exception {
        assertThat(HandlerMethodMatcherTest.TestServiceComponent.class, hasMethodThatHandlesAction("testA", "testA"));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchMethodWithWrongHandlesAnnotation() throws Exception {
        assertThat(HandlerMethodMatcherTest.TestServiceComponent.class, hasMethodThatHandlesAction("testA", "wrongActionName"));
    }

    @ServiceComponent(COMMAND_API)
    public static class TestServiceComponent {

        @Handles("testA")
        public void testA(final JsonEnvelope command) {

        }

    }

}
