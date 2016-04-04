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
import uk.gov.justice.services.messaging.Envelope;
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
import static uk.gov.justice.services.core.handler.HandlerUtil.findHandlerMethods;

@RunWith(MockitoJUnitRunner.class)
public class HandlerMethodTest {

    @Mock
    private AsynchronousCommandHandler asynchronousCommandHandler;

    @Mock
    private SynchronousCommandHandler synchronousCommandHandler;


    private Envelope envelope;

    @Before
    public void setup() throws Exception {
        envelope = testEnvelope("envelope.json");
    }

    @Test
    public void shouldExecuteAsynchronousHandlerMethod() throws Exception {
        Object result = asyncHandlerInstance().execute(envelope);
        verify(asynchronousCommandHandler).handles(envelope);
        assertThat(result, nullValue());
    }

    @Test
    public void shouldExecuteSynchronousHandlerMethod() throws Exception {
        when(synchronousCommandHandler.handles(envelope)).thenReturn(envelope);
        Object result = syncHandlerInstance().execute(envelope);
        assertThat(result, sameInstance(envelope));
    }

    @Test(expected = HandlerExecutionException.class)
    public void shouldThrowHandlerExecutionExceptionIfExceptionThrown() throws Exception {
        doThrow(new RuntimeException()).when(asynchronousCommandHandler).handles(envelope);
        asyncHandlerInstance().execute(envelope);
    }

    @Test
    public void shouldReturnStringDescriptionOfHandlerInstanceAndMethod() {
        assertThat(asyncHandlerInstance().toString(), notNullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithNullHandlerInstance() {
        new HandlerMethod(null, method(AsynchronousCommandHandler.class, "handles"), Void.TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithNullMethod() {
        new HandlerMethod(asynchronousCommandHandler, null, Void.TYPE);
    }

    @Test(expected = InvalidHandlerException.class)
    public void shouldThrowExceptionWithSynchronousMethod() {
        new HandlerMethod(asynchronousCommandHandler, method(AsynchronousCommandHandler.class, "handlesSync"), Void.TYPE);
    }

    @Test(expected = InvalidHandlerException.class)
    public void shouldThrowExceptionWithAsynchronousMethod() {
        new HandlerMethod(synchronousCommandHandler, method(SynchronousCommandHandler.class, "handlesAsync"), Envelope.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldOnlyAcceptVoidOrEnvelopeReturnTypes() {
        new HandlerMethod(synchronousCommandHandler, method(SynchronousCommandHandler.class, "handles"), Object.class);
    }

    private HandlerMethod asyncHandlerInstance() {
        return new HandlerMethod(asynchronousCommandHandler, method(AsynchronousCommandHandler.class, "handles"), Void.TYPE);
    }

    private HandlerMethod syncHandlerInstance() {
        return new HandlerMethod(synchronousCommandHandler, method(SynchronousCommandHandler.class, "handles"), Envelope.class);
    }

    private Method method(final Class<?> clazz, final String name) {
        return findHandlerMethods(clazz, Handles.class).stream()
                .filter(m -> name.equals(m.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(format("Cannot find method with name %s", name)));
    }

    private Envelope testEnvelope(String fileName) throws IOException {
        String jsonString = Resources.toString(Resources.getResource("json/" + fileName), Charset.defaultCharset());
        return new JsonObjectEnvelopeConverter().asEnvelope(new StringToJsonObjectConverter().convert(jsonString));
    }

    public static class AsynchronousCommandHandler {

        @Handles("test-context.commands.create-something")
        public void handles(final Envelope envelope) {
        }

        @Handles("test-context.commands.create-something-else")
        public Envelope handlesSync(final Envelope envelope) {
            return envelope;
        }
    }

    public static class SynchronousCommandHandler {

        @Handles("test-context.commands.create-something")
        public Envelope handles(final Envelope envelope) {
            return envelope;
        }

        @Handles("test-context.commands.create-something-else")
        public void handlesAsync(final Envelope envelope) {
        }
    }
}
