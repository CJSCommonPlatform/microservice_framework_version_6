package uk.gov.justice.services.core.dispatcher;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.handler.TestPojo;
import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.core.handler.registry.HandlerRegistry;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.common.envelope.TestEnvelopeRecorder;

import java.util.List;

import javax.json.JsonValue;

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

    private final Metadata metadata = metadataBuilder()
            .withId(randomUUID())
            .withName(ACTION_NAME).build();

    private Dispatcher dispatcher;

    private HandlerRegistry handlerRegistry;

    @Before
    public void setup() {
        handlerRegistry = new HandlerRegistry(logger);
        dispatcher = new Dispatcher(handlerRegistry, new EnvelopePayloadTypeConverter(new ObjectMapperProducer().objectMapper()), new JsonEnvelopeRepacker());
    }

    @Test
    public void shouldDispatchAsynchronouslyToAValidHandler() throws Exception {
        final AsynchronousTestHandler asynchronousTestHandler = new AsynchronousTestHandler();

        final JsonValue payload = createObjectBuilder()
                .add("aField", "aValue").build();

        final JsonEnvelope envelope = envelopeFrom(metadata, payload);

        dispatcher.register(asynchronousTestHandler);
        dispatcher.dispatch(envelope);

        final List<JsonEnvelope> dispatchedEnvelopes = asynchronousTestHandler.recordedEnvelopes();
        assertThat(dispatchedEnvelopes, hasSize(1));
        assertThat(dispatchedEnvelopes.get(0), equalTo(envelope));
    }

    @Test
    public void shouldDispatchSynchronouslyToAValidHandler() throws Exception {
        final SynchronousTestHandler synchronousTestHandler = new SynchronousTestHandler();

        final JsonValue payload = createObjectBuilder()
                .add("aField", "aValue").build();

        final JsonEnvelope envelope = envelopeFrom(metadata, payload);

        dispatcher.register(synchronousTestHandler);
        dispatcher.dispatch(envelope);

        assertThat(synchronousTestHandler.envelope, equalTo(envelope));
    }

    @Test
    public void shouldDispatchAsynchronouslyToPojoHandler() {

        AsynchronousPojoCommandApi asynchronousPojoCommandApi = new AsynchronousPojoCommandApi();
        dispatcher.register(asynchronousPojoCommandApi);

        final String payloadId = "3f47ab7e-aecc-4cec-9246-c32066ef5ba1";
        final String payloadName = "payload name";
        final long payloadVersion = 200L;

        final JsonValue payload = withPayloadOfTestPojo(payloadId, payloadName, payloadVersion);

        final JsonEnvelope envelope = envelopeFrom(metadata, payload);

        dispatcher.dispatch(envelope);
    }

    @Test
    public void shouldDispatchSynchronouslyToPojoHandler() {

        final SynchronousPojoCommandApi synchronousPojoCommandApi = new SynchronousPojoCommandApi();
        dispatcher.register(synchronousPojoCommandApi);

        final String payloadId = "3f47ab7e-aecc-4cec-9246-c32066ef5ba1";
        final String payloadName = "payload name";
        final long payloadVersion = 200L;

        final JsonValue payload = withPayloadOfTestPojo(payloadId, payloadName, payloadVersion);

        final JsonEnvelope envelope = envelopeFrom(metadata, payload);

        final JsonEnvelope response = dispatcher.dispatch(envelope);

        assertThat(response.payloadAsJsonObject().getString("payloadId"), is(payloadId));
        assertThat(response.payloadAsJsonObject().getString("payloadName"), is(payloadName));
        assertThat(response.payloadAsJsonObject().getInt("payloadVersion"), is(200));
    }


    private JsonValue withPayloadOfTestPojo(String payloadId, String payloadName, long payloadVersion) {
        return createObjectBuilder()
                    .add("payloadId", payloadId)
                    .add("payloadName", payloadName)
                    .add("payloadVersion", payloadVersion)
                    .build();
    }

    @Test(expected = MissingHandlerException.class)
    public void shouldThrowExceptionIfNoHandlerExists() throws Exception {

        final String payloadId = "3f47ab7e-aecc-4cec-9246-c32066ef5ba1";
        final String payloadName = "payload name";
        final long payloadVersion = 200L;

        final JsonValue payload = withPayloadOfTestPojo(payloadId, payloadName, payloadVersion);

        final JsonEnvelope envelope = envelopeFrom(metadata, payload);


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

    @ServiceComponent(COMMAND_API)
    public static class AsynchronousPojoCommandApi {

        @Handles(ACTION_NAME)
        public void handles(final Envelope<TestPojo> pojo) {
            assertThat(pojo.payload(), isA(TestPojo.class));
        }
    }


    @ServiceComponent(COMMAND_API)
    public static class SynchronousPojoCommandApi {

        @Handles(ACTION_NAME)
        public Envelope<TestPojo> handles(final Envelope<TestPojo> pojo) {
            assertThat(pojo.payload(), isA(TestPojo.class));
            return pojo;
        }
    }
}
