package uk.gov.justice.services.test.utils.core.matchers;

import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.test.utils.core.matchers.SenderPassThroughMatcher.hasSenderPassThroughMethod;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.junit.Test;

/**
 * Unit tests for the {@link SenderPassThroughMatcher} class.
 */
public class SenderPassThroughMatcherTest {

    @Test
    public void shouldMatchAPassThroughCommandMethodThatHasHandlesAnnotation() throws Exception {
        assertThat(TestServiceComponent.class, hasSenderPassThroughMethod("testA"));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchIfMethodDoesNotCallSender() throws Exception {
        assertThat(TestServiceComponent.class, hasSenderPassThroughMethod("testB"));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchIfMethodCallsSenderMoreThanOnce() throws Exception {
        assertThat(TestServiceComponent.class, hasSenderPassThroughMethod("testC"));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchIfMethodDoesNotSendCommand() throws Exception {
        assertThat(TestServiceComponent.class, hasSenderPassThroughMethod("testD"));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchAPassThroughCommandMethodIfRequesterButExpectedSender() throws Exception {
        assertThat(TestServiceComponent.class, hasSenderPassThroughMethod("testE"));
    }

    @ServiceComponent(COMMAND_API)
    public static class TestServiceComponent {

        @Inject
        Sender sender;

        @Inject
        Requester requester;

        @Handles("testA")
        public void testA(final JsonEnvelope command) {
            sender.send(command);
        }

        @Handles("testB")
        public void testB(final JsonEnvelope command) {
        }

        @Handles("testC")
        public void testC(final JsonEnvelope command) {
            sender.send(command);
            sender.send(command);
        }

        @Handles("testD")
        public void testE(final JsonEnvelope command) {
            sender.send(null);
        }


        @Handles("testE")
        public JsonEnvelope testJ(final JsonEnvelope command) {
            return requester.request(command);
        }
    }
}
