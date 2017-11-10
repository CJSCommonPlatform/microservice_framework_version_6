package uk.gov.justice.services.core.handler;

import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.handler.Handlers.handlerMethodsFrom;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.Method;

import javax.json.JsonValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HandlerMethodInvokerTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private HandlerMethodInvoker handlerMethodInvoker;

    private JsonEnvelope envelope;

    @Before
    public void setup() throws Exception {
        final String jsonString = Resources.toString(Resources.getResource("json/" + "envelope.json"), defaultCharset());
        envelope = new DefaultJsonObjectEnvelopeConverter().asEnvelope(new StringToJsonObjectConverter().convert(jsonString));
    }

    @Test
    public void shouldReturnJsonEnvelope() throws Exception {
        final JsonEnvelopeHandler jsonEnvelopeHandler = new JsonEnvelopeHandler();

        final Envelope result = (Envelope) handlerMethodInvoker.invoke(
                jsonEnvelopeHandler,
                method(jsonEnvelopeHandler, "handles"),
                envelope);

        assertThat(result.payload(), is(instanceOf(JsonValue.class)));
        assertThat(result.payload().toString(), is(envelope.payloadAsJsonObject().toString()));
    }

    @Test
    public void shouldReturnPojoEnvelope() throws Exception {
        final PojoEnvelopeHandler pojoEnvelopeHandler = new PojoEnvelopeHandler();
        final TestPojo pojo = mock(TestPojo.class);

        when(objectMapper.readValue(envelope.payloadAsJsonObject().toString(), TestPojo.class)).thenReturn(pojo);

        final Envelope result = (Envelope) handlerMethodInvoker.invoke(
                pojoEnvelopeHandler,
                method(pojoEnvelopeHandler, "handles"),
                envelope);

        assertThat(result.payload(), is(instanceOf(TestPojo.class)));
        assertThat(result.payload(), is(pojo));
    }

    private Method method(final Object object, final String methodName) {
        return handlerMethodsFrom(object).stream()
                .filter(m -> methodName.equals(m.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(format("Cannot find method with name %s", methodName)));
    }

    public static class JsonEnvelopeHandler {

        @Handles("test-context.command.create-something-else")
        public JsonEnvelope handles(final JsonEnvelope envelope) {
            return envelope;
        }
    }

    public static class PojoEnvelopeHandler {

        @Handles("test-context.command.create-something")
        public Envelope<TestPojo> handles(final Envelope<TestPojo> pojo) {
            return pojo;
        }
    }
}