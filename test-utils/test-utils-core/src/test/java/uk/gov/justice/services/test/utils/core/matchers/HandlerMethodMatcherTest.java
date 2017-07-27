package uk.gov.justice.services.test.utils.core.matchers;

import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import org.junit.Test;

public class HandlerMethodMatcherTest {

    @Test
    public void shouldMatchMethod() throws Exception {
        assertThat(TestServiceComponent.class, method("testA"));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchMissingMethod() throws Exception {
        assertThat(TestServiceComponent.class, method("missingMethodName"));
    }

    @Test
    public void shouldMatchMethodWithHandlesAnnotation() throws Exception {
        assertThat(TestServiceComponent.class, method("testA").thatHandles("testA"));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchMethodWithWrongHandlesAnnotation() throws Exception {
        assertThat(TestServiceComponent.class, method("testA").thatHandles("wrongActionName"));
    }

    @Test
    public void shouldMatchAPassThroughCommandMethodThatHasHandlesAnnotation() throws Exception {
        assertThat(TestServiceComponent.class, method("testA").thatHandles("testA").withSenderPassThrough());
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchIfMethodDoesNotCallSender() throws Exception {
        assertThat(TestServiceComponent.class, method("testB").thatHandles("testB").withSenderPassThrough());
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchIfMethodCallsSenderMoreThanOnce() throws Exception {
        assertThat(TestServiceComponent.class, method("testC").thatHandles("testC").withSenderPassThrough());
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchIfMethodDoesNotHaveJsonEnvelopeArgument() throws Exception {
        assertThat(TestServiceComponent.class, method("testD"));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchIfMethodDoesNotSendCommand() throws Exception {
        assertThat(TestServiceComponent.class, method("testE").thatHandles("testE").withSenderPassThrough());
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchAPassThroughCommandMethodThatDoesNotHaveHandlesAnnotation() throws Exception {
        assertThat(TestServiceComponent.class, method("testG").thatHandles("testG"));
    }

    @Test
    public void shouldMatchAPassThroughRequesterMethod() throws Exception {
        assertThat(TestServiceComponent.class, method("testF").thatHandles("testF").withRequesterPassThrough());
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchAPassThroughCommandMethodThatDoesNotExist() throws Exception {
        assertThat(TestServiceComponent.class, method("notExist"));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchAPassThroughCommandMethodIfIncorrectArgumentType() throws Exception {
        assertThat(TestServiceComponent.class, method("testH"));
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchAPassThroughCommandMethodIfSenderButExpectedRequester() throws Exception {
        assertThat(TestServiceComponent.class, method("testI").withRequesterPassThrough());
    }

    @Test(expected = AssertionError.class)
    public void shouldNotMatchAPassThroughCommandMethodIfRequesterButExpectedSender() throws Exception {
        assertThat(TestServiceComponent.class, method("testJ").withSenderPassThrough());
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
        public void testD() {
            sender.send(null);
        }

        @Handles("testE")
        public void testE(final JsonEnvelope command) {
            sender.send(null);
        }

        @Handles("testF")
        public JsonEnvelope testF(final JsonEnvelope query) {
            return requester.request(query);
        }

        public void testG(final JsonEnvelope command) {
            sender.send(command);
        }

        @Handles("testH")
        public void testH(final String command) {
            sender.send(envelope().build());
        }

        @Handles("testI")
        public void testI(final JsonEnvelope query) {
            sender.send(query);
        }

        @Handles("testJ")
        public JsonEnvelope testJ(final JsonEnvelope command) {
            return requester.request(command);
        }
    }
}
