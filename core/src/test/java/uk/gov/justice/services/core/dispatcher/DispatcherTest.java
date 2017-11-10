package uk.gov.justice.services.core.dispatcher;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.handler.HandlerMethodInvoker;
import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.core.handler.registry.HandlerRegistry;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.common.envelope.TestEnvelopeRecorder;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherTest {

    private static final String ACTION_NAME = "test.some-action";

    @Mock
    private Logger logger;

    @Mock
    private JsonEnvelope envelope;

    @Mock
    private Metadata metadata;

    private Dispatcher dispatcher;

    private HandlerRegistry handlerRegistry;

    @Before
    public void setup() {
        handlerRegistry = new HandlerRegistry(new HandlerMethodInvoker(new ObjectMapperProducer().objectMapper()), logger);
        dispatcher = new Dispatcher(handlerRegistry);

        when(envelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(ACTION_NAME);
    }

    @Test
    public void shouldDispatchAsynchronouslyToAValidHandler() throws Exception {
        final AsynchronousTestHandler asynchronousTestHandler = new AsynchronousTestHandler();

        dispatcher.register(asynchronousTestHandler);
        dispatcher.dispatch(envelope);

        final List<JsonEnvelope> dispatchedEnvelopes = asynchronousTestHandler.recordedEnvelopes();
        assertThat(dispatchedEnvelopes, hasSize(1));
        assertThat(dispatchedEnvelopes.get(0), equalTo(envelope));
    }

    @Test
    public void shouldDispatchSynchronouslyToAValidHandler() throws Exception {
        final SynchronousTestHandler synchronousTestHandler = new SynchronousTestHandler();

        dispatcher.register(synchronousTestHandler);
        dispatcher.dispatch(envelope);

        assertThat(synchronousTestHandler.envelope, equalTo(envelope));
    }

    @Test(expected = MissingHandlerException.class)
    public void shouldThrowExceptionIfNoHandlerExists() throws Exception {
        dispatcher.dispatch(envelope);
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
