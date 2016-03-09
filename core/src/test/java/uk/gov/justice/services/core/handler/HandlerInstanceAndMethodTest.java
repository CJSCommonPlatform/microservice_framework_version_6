package uk.gov.justice.services.core.handler;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.JsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.handler.exception.HandlerExecutionException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HandlerInstanceAndMethodTest {

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

    @Test
    public void shouldNotReturnNullWithNullHandlerInstance() {
        assertThat(nullHandlerInstanceWithMethod().toString(), notNullValue());
    }

    @Test
    public void shouldNotReturnNullWithNullMethod() {
        assertThat(handlerInstanceWithNullMethod().toString(), notNullValue());
    }

    private HandlerInstanceAndMethod handlerInstanceWithNullMethod() {
        return new HandlerInstanceAndMethod(commandHandler, null);
    }

    private HandlerInstanceAndMethod nullHandlerInstanceWithMethod() {
        return new HandlerInstanceAndMethod(null, methods().get(0));
    }

    private HandlerInstanceAndMethod handlerInstanceWithMethod() {
        return new HandlerInstanceAndMethod(commandHandler, methods().get(0));
    }

    private List<Method> methods() {
        return HandlerUtil.findHandlerMethods(CommandHandler.class, Handles.class);
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

    }
}
