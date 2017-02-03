package uk.gov.justice.services.test.utils.core.matchers;

import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.test.utils.core.matchers.RequesterPassThroughMatcher.hasRequesterPassThroughMethod;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.junit.Test;

/**
 * Unit tests for the {@link RequesterPassThroughMatcher} class.
 */
public class RequesterPassThroughMatcherTest {

    @Test
    public void shouldMatchAPassThroughCommandMethodThatHasHandlesAnnotation() throws Exception {
        assertThat(TestServiceComponent.class, hasRequesterPassThroughMethod("testA"));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchIfMethodDoesNotCallRequester() throws Exception {
        assertThat(TestServiceComponent.class, hasRequesterPassThroughMethod("testB"));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchIfMethodCallsRequesterMoreThanOnce() throws Exception {
        assertThat(TestServiceComponent.class, hasRequesterPassThroughMethod("testC"));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchIfMethodDoesNotMakeRequest() throws Exception {
        assertThat(TestServiceComponent.class, hasRequesterPassThroughMethod("testD"));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchAPassThroughCommandMethodIfSenderButExpectedRequester() throws Exception {
        assertThat(TestServiceComponent.class, hasRequesterPassThroughMethod("testE"));
    }

    @ServiceComponent(COMMAND_API)
    public static class TestServiceComponent {

        @Inject
        Sender sender;

        @Inject
        Requester requester;

        @Handles("testA")
        public JsonEnvelope testA(final JsonEnvelope query) {
            return requester.request(query);
        }

        @Handles("testB")
        public void testB(final JsonEnvelope query) {
        }

        @Handles("testC")
        public JsonEnvelope testC(final JsonEnvelope query) {
            requester.request(query);
            return requester.request(query);
        }

        @Handles("testD")
        public void testE(final JsonEnvelope query) {
            requester.request(null);
        }


        @Handles("testE")
        public void testJ(final JsonEnvelope query) {
            sender.send(query);
        }
    }
}
