package uk.gov.justice.services.core.dispatcher;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AccessControlService;
import uk.gov.justice.services.core.accesscontrol.AccessControlViolation;
import uk.gov.justice.services.core.accesscontrol.AccessControlViolationException;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.core.handler.registry.HandlerRegistry;
import uk.gov.justice.services.core.util.TestEnvelopeRecorder;
import uk.gov.justice.services.event.buffer.api.EventBufferService;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherTest {

    private static final String ACTION_NAME = "test.some-action";

    @Mock
    private JsonEnvelope envelope;

    @Mock
    private Metadata metadata;

    @Mock
    private EventBufferService eventBufferService;

    @Spy
    private HandlerRegistry handlerRegistry;

    @Mock
    private AccessControlService accessControlService;

    @Mock
    private AccessControlFailureMessageGenerator accessControlFailureMessageGenerator;

    private Dispatcher dispatcher;

    @Before
    public void setup() {
        dispatcher = new Dispatcher(handlerRegistry, Optional.of(accessControlService), eventBufferService, accessControlFailureMessageGenerator);

        when(envelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(ACTION_NAME);
    }

    @Test
    public void shouldDispatchAsynchronouslyToAValidHandler() throws Exception {
        final AsynchronousTestHandler asynchronousTestHandler = new AsynchronousTestHandler();

        when(eventBufferService.currentOrderedEventsWith(envelope)).thenReturn(Stream.of(envelope));
        when(accessControlService.checkAccessControl(envelope)).thenReturn(empty());

        dispatcher.register(asynchronousTestHandler);
        dispatcher.asynchronousDispatch(envelope);

        final List<JsonEnvelope> dispatchedEnvelopes = asynchronousTestHandler.recordedEnvelopes();
        assertThat(dispatchedEnvelopes, hasSize(1));
        assertThat(dispatchedEnvelopes.get(0), equalTo(envelope));
    }

    @Test
    public void shouldDispatchMultipleBufferedEvents() throws Exception {
        final AsynchronousTestHandler asynchronousTestHandler = new AsynchronousTestHandler();

        JsonEnvelope bufferedEnvelope1 = envelope().with(metadataWithRandomUUID(ACTION_NAME)).build();
        JsonEnvelope bufferedEnvelope2 = envelope().with(metadataWithRandomUUID(ACTION_NAME)).build();

        when(eventBufferService.currentOrderedEventsWith(envelope))
                .thenReturn(Stream.of(envelope, bufferedEnvelope1, bufferedEnvelope2));

        when(accessControlService.checkAccessControl(any(JsonEnvelope.class))).thenReturn(empty());

        dispatcher.register(asynchronousTestHandler);
        dispatcher.asynchronousDispatch(envelope);

        assertThat(asynchronousTestHandler.recordedEnvelopes(), contains(envelope, bufferedEnvelope1, bufferedEnvelope2));
    }

    @Test
    public void shouldNotDispatchAnyEnvelopsIfBufferReturnsEmptyCollection() throws Exception {
        final AsynchronousTestHandler asynchronousTestHandler = new AsynchronousTestHandler();


        when(eventBufferService.currentOrderedEventsWith(envelope))
                .thenReturn(Stream.empty());

        when(accessControlService.checkAccessControl(any(JsonEnvelope.class))).thenReturn(empty());

        dispatcher.register(asynchronousTestHandler);
        dispatcher.asynchronousDispatch(envelope);

        assertThat(asynchronousTestHandler.recordedEnvelopes(), Matchers.empty());
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

        when(eventBufferService.currentOrderedEventsWith(envelope)).thenReturn(Stream.of(envelope));
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
        when(eventBufferService.currentOrderedEventsWith(envelope)).thenReturn(Stream.of(envelope));
        when(accessControlService.checkAccessControl(envelope)).thenReturn(empty());

        dispatcher.asynchronousDispatch(envelope);
    }

    @Test(expected = MissingHandlerException.class)
    public void shouldThrowExceptionIfNoSynchronousHandlerExists() throws Exception {

        when(accessControlService.checkAccessControl(envelope)).thenReturn(empty());

        dispatcher.synchronousDispatch(envelope);
    }

    @Test
    public void shouldSkipAccessControlIfServiceNotProvided() throws Exception {

        dispatcher = new Dispatcher(handlerRegistry, Optional.empty(), eventBufferService, accessControlFailureMessageGenerator);

        final SynchronousTestHandler synchronousTestHandler = new SynchronousTestHandler();

        dispatcher.register(synchronousTestHandler);
        dispatcher.synchronousDispatch(envelope);

        assertThat(synchronousTestHandler.envelope, equalTo(envelope));
        verifyZeroInteractions(accessControlService);
    }

    @ServiceComponent(COMMAND_API)
    public static class AsynchronousTestHandler extends TestEnvelopeRecorder {

        @Handles(ACTION_NAME)
        public void handle(JsonEnvelope envelope) {
            record(envelope);
        }
    }

    @ServiceComponent(COMMAND_API)
    public static class SynchronousTestHandler {

        JsonEnvelope envelope;

        @Handles(ACTION_NAME)
        public JsonEnvelope handle(JsonEnvelope envelope) {
            this.envelope = envelope;
            return envelope;
        }
    }

    @ServiceComponent(QUERY_API)
    public static class TestQueryHandler {

        JsonEnvelope envelope;

        @Handles(ACTION_NAME)
        public JsonEnvelope handle(JsonEnvelope envelope) {
            this.envelope = envelope;
            return envelope;
        }

    }
}
