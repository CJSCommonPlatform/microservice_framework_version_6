package uk.gov.justice.services.core.handler;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.core.handler.HandlerUtil.findHandlerMethods;

import uk.gov.justice.services.common.converter.JsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.handler.exception.HandlerExecutionException;
import uk.gov.justice.services.core.handler.registry.exception.InvalidHandlerException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AsynchronousHandlerMethodTest {

    @Mock
    private CommandHandler commandHandler;

    private Envelope envelope;

    @Before
    public void setup() throws Exception {
        envelope = testEnvelope("envelope.json");
    }

    @Test
    public void shouldExecuteHandlerMethod() throws Exception {
        handlerInstanceWithMethod().execute(envelope);
        verify(commandHandler).handles(envelope);
    }

    @Test(expected = HandlerExecutionException.class)
    public void shouldThrowHandlerExecutionExceptionIfExceptionThrown() throws Exception {
        doThrow(new RuntimeException()).when(commandHandler).handles(envelope);
        handlerInstanceWithMethod().execute(envelope);
    }

    @Test
    public void shouldReturnStringDescriptionOfHandlerInstanceAndMethod() {
        assertThat(handlerInstanceWithMethod().toString(), notNullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithNullHandlerInstance() {
        new AsynchronousHandlerMethod(null, method("handles"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithNullMethod() {
        new AsynchronousHandlerMethod(commandHandler, null);
    }

    @Test(expected = InvalidHandlerException.class)
    public void shouldThrowExceptionWithSynchronousMethod() {
        new AsynchronousHandlerMethod(commandHandler, method("handlesSync"));
    }

    private AsynchronousHandlerMethod handlerInstanceWithMethod() {
        return new AsynchronousHandlerMethod(commandHandler, method("handles"));
    }

    private Method method(final String name) {
        return findHandlerMethods(CommandHandler.class, Handles.class).stream()
                .filter(m -> name.equals(m.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(format("Cannot find method with name %s", name)));
    }

    private Envelope testEnvelope(String fileName) throws IOException {
        JsonObjectConverter jsonObjectConverter = new JsonObjectConverter();

        String jsonString = Resources.toString(Resources.getResource("json/" + fileName), Charset.defaultCharset());
        return new JsonObjectEnvelopeConverter().asEnvelope(jsonObjectConverter.fromString(jsonString));
    }

    public static class CommandHandler {

        @Handles("test-context.commands.create-something")
        public void handles(final Envelope envelope) {
        }

        @Handles("test-context.commands.create-something-else")
        public Envelope handlesSync(final Envelope envelope) {
            return envelope;
        }
    }
}
