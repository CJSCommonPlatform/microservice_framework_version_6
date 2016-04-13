package uk.gov.justice.services.core.handler;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.handler.exception.HandlerExecutionException;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.handler.Handlers.handlerMethodsFrom;

@RunWith(MockitoJUnitRunner.class)
public class HandlerMethodTest {

    @Mock
    private AsynchronousCommandHandler asynchronousCommandHandler;

    @Mock
    private SynchronousCommandHandler synchronousCommandHandler;


    private JsonEnvelope jsonEnvelope;

    @Before
    public void setup() throws Exception {
        jsonEnvelope = testEnvelope("envelope.json");
    }

    @Test
    public void shouldExecuteAsynchronousHandlerMethod() throws Exception {
        Object result = asyncHandlerInstance().execute(jsonEnvelope);
        verify(asynchronousCommandHandler).handles(jsonEnvelope);
        assertThat(result, nullValue());
    }

    @Test
    public void shouldExecuteSynchronousHandlerMethod() throws Exception {
        when(synchronousCommandHandler.handles(jsonEnvelope)).thenReturn(jsonEnvelope);
        Object result = syncHandlerInstance().execute(jsonEnvelope);
        assertThat(result, sameInstance(jsonEnvelope));
    }

    @Test(expected = HandlerExecutionException.class)
    public void shouldThrowHandlerExecutionExceptionIfExceptionThrown() throws Exception {
        doThrow(new RuntimeException()).when(asynchronousCommandHandler).handles(jsonEnvelope);
        asyncHandlerInstance().execute(jsonEnvelope);
    }

    @Test
    public void shouldReturnStringDescriptionOfHandlerInstanceAndMethod() {
        assertThat(asyncHandlerInstance().toString(), notNullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithNullHandlerInstance() {
        new HandlerMethod(null, method(new AsynchronousCommandHandler(), "handles"), Void.TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithNullMethod() {
        new HandlerMethod(asynchronousCommandHandler, null, Void.TYPE);
    }

    @Test(expected = InvalidHandlerException.class)
    public void shouldThrowExceptionWithSynchronousMethod() {
        new HandlerMethod(asynchronousCommandHandler, method(new AsynchronousCommandHandler(), "handlesSync"), Void.TYPE);
    }

    @Test(expected = InvalidHandlerException.class)
    public void shouldThrowExceptionWithAsynchronousMethod() {
        new HandlerMethod(synchronousCommandHandler, method(new SynchronousCommandHandler(), "handlesAsync"), JsonEnvelope.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldOnlyAcceptVoidOrEnvelopeReturnTypes() {
        new HandlerMethod(synchronousCommandHandler, method(new SynchronousCommandHandler(), "handles"), Object.class);
    }

    private HandlerMethod asyncHandlerInstance() {
        return new HandlerMethod(asynchronousCommandHandler, method(new AsynchronousCommandHandler(), "handles"), Void.TYPE);
    }

    private HandlerMethod syncHandlerInstance() {
        return new HandlerMethod(synchronousCommandHandler, method(new SynchronousCommandHandler(), "handles"), JsonEnvelope.class);
    }

    private Method method(final Object object, final String methofName) {
        return handlerMethodsFrom(object).stream()
                .filter(m -> methofName.equals(m.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(format("Cannot find method with name %s", methofName)));
    }

    private JsonEnvelope testEnvelope(String fileName) throws IOException {
        String jsonString = Resources.toString(Resources.getResource("json/" + fileName), Charset.defaultCharset());
        return new JsonObjectEnvelopeConverter().asEnvelope(new StringToJsonObjectConverter().convert(jsonString));
    }

    public static class AsynchronousCommandHandler {

        @Handles("test-context.commands.create-something")
        public void handles(final JsonEnvelope jsonEnvelope) {
        }

        @Handles("test-context.commands.create-something-else")
        public JsonEnvelope handlesSync(final JsonEnvelope jsonEnvelope) {
            return jsonEnvelope;
        }
    }

    public static class SynchronousCommandHandler {

        @Handles("test-context.commands.create-something")
        public JsonEnvelope handles(final JsonEnvelope jsonEnvelope) {
            return jsonEnvelope;
        }

        @Handles("test-context.commands.create-something-else")
        public void handlesAsync(final JsonEnvelope jsonEnvelope) {
        }
    }
}
