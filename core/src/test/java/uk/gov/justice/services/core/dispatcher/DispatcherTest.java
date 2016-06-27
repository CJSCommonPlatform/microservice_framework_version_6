package uk.gov.justice.services.core.dispatcher;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;

import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AccessControlService;
import uk.gov.justice.services.core.accesscontrol.AccessControlViolation;
import uk.gov.justice.services.core.accesscontrol.AccessControlViolationException;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.core.handler.registry.HandlerRegistry;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherTest {

    private static final String NAME = "test.command.do-something";

    @Mock
    private JsonEnvelope envelope;

    @Mock
    private Metadata metadata;

    @Spy
    private HandlerRegistry handlerRegistry;

    @Mock
    private AccessControlService accessControlService;

    @Mock
    private AccessControlFailureMessageGenerator accessControlFailureMessageGenerator;

    @InjectMocks
    private Dispatcher dispatcher;

    @Before
    public void setup() {
        when(envelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(NAME);
    }

    @Test
    public void shouldDispatchAsynchronouslyToAValidHandler() throws Exception {
        final AsynchronousTestHandler asynchronousTestHandler = new AsynchronousTestHandler();

        when(accessControlService.checkAccessControl(envelope)).thenReturn(empty());

        dispatcher.register(asynchronousTestHandler);
        dispatcher.asynchronousDispatch(envelope);

        assertThat(asynchronousTestHandler.envelope, equalTo(envelope));
    }

    @Test
    public void shouldDispatchSynchronouslyToAValidHandler() throws Exception {
        final SynchronousTestHandler synchronousTestHandler = new SynchronousTestHandler();

        when(accessControlService.checkAccessControl(envelope)).thenReturn(empty());

        dispatcher.register(synchronousTestHandler);
        dispatcher.synchronousDispatch(envelope);

        assertThat(synchronousTestHandler.envelope, equalTo(envelope));
    }

    @Test
    public void shouldThrowAnAccessControlServiceExceptionIfTheAccessControlFailsForAsynchronousAccess()
                    throws Exception {

        final String errorMessage = "error message";
        final AccessControlViolation accessControlViolation = new AccessControlViolation("Ooops");
        final AsynchronousTestHandler asynchronousTestHandler = new AsynchronousTestHandler();

        when(accessControlService.checkAccessControl(envelope)).thenReturn(of(
                        accessControlViolation));
        when(accessControlFailureMessageGenerator.errorMessageFrom(envelope, accessControlViolation))
                        .thenReturn(errorMessage);

        dispatcher.register(asynchronousTestHandler);

        try {
            dispatcher.asynchronousDispatch(envelope);
            fail();
        } catch (AccessControlViolationException expected) {
            assertThat(expected.getMessage(), is(errorMessage));
        }
    }

    @Test
    public void shouldThrowAnAccessControlServiceExceptionIfTheAccessControlFailsForSynchronousAccess()
                    throws Exception {

        final String errorMessage = "error message";
        final AccessControlViolation accessControlViolation = new AccessControlViolation("Ooops");
        final SynchronousTestHandler asynchronousTestHandler = new SynchronousTestHandler();

        when(accessControlService.checkAccessControl(envelope)).thenReturn(of(
                        accessControlViolation));
        when(accessControlFailureMessageGenerator.errorMessageFrom(envelope, accessControlViolation))
                        .thenReturn(errorMessage);

        dispatcher.register(asynchronousTestHandler);

        try {
            dispatcher.synchronousDispatch(envelope);
            fail();
        } catch (AccessControlViolationException expected) {
            assertThat(expected.getMessage(), is(errorMessage));
        }
    }

    @Test(expected = MissingHandlerException.class)
    public void shouldThrowExceptionIfNoAsynchronousHandlerExists() throws Exception {

        when(accessControlService.checkAccessControl(envelope)).thenReturn(empty());

        dispatcher.asynchronousDispatch(envelope);
    }

    @Test(expected = MissingHandlerException.class)
    public void shouldThrowExceptionIfNoSynchronousHandlerExists() throws Exception {

        when(accessControlService.checkAccessControl(envelope)).thenReturn(empty());

        dispatcher.synchronousDispatch(envelope);
    }

    @ServiceComponent(COMMAND_API)
    public static class AsynchronousTestHandler {

        JsonEnvelope envelope;

        @Handles(NAME)
        public void handle(JsonEnvelope envelope) {
            this.envelope = envelope;
        }
    }

    @ServiceComponent(COMMAND_API)
    public static class SynchronousTestHandler {

        JsonEnvelope envelope;

        @Handles(NAME)
        public JsonEnvelope handle(JsonEnvelope envelope) {
            this.envelope = envelope;
            return envelope;
        }
    }

    @ServiceComponent(QUERY_API)
    public static class TestQueryHandler {

        JsonEnvelope envelope;

        @Handles(NAME)
        public JsonEnvelope handle(JsonEnvelope envelope) {
            this.envelope = envelope;
            return envelope;
        }

    }
}
