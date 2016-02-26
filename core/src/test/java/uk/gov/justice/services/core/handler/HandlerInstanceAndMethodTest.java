package uk.gov.justice.services.core.handler;

import com.google.common.io.Resources;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.handler.exception.HandlerExecutionException;
import uk.gov.justice.services.core.util.JsonObjectConverter;
import uk.gov.justice.services.messaging.Envelope;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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

    @Test
    public void shouldExecuteHandlerMethod() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
        Envelope envelope = testEnvelope("envelope.json");
        List<Method> methods = HandlerUtil.findHandlerMethods(CommandHandler.class, Handles.class);
        HandlerInstanceAndMethod handlerInstanceAndMethod = new HandlerInstanceAndMethod(commandHandler, methods.get(0));
        assertThat(handlerInstanceAndMethod, notNullValue());

        handlerInstanceAndMethod.execute(envelope);
        verify(commandHandler).handler1(envelope);
    }

    @Test(expected = HandlerExecutionException.class)
    public void shouldThrowExceptionWithExceptionFromuteThrowsException() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
        Envelope envelope = testEnvelope("envelope.json");
        List<Method> methods = HandlerUtil.findHandlerMethods(CommandHandler.class, Handles.class);
        HandlerInstanceAndMethod handlerInstanceAndMethod = new HandlerInstanceAndMethod(commandHandler, methods.get(0));
        assertThat(handlerInstanceAndMethod, CoreMatchers.notNullValue());

        doThrow(new RuntimeException()).when(commandHandler).handler1(envelope);
        handlerInstanceAndMethod.execute(envelope);
    }

    @Test
    public void shouldReturnStringDescriptionOfHandlerInstanceAndMethod() {
        List<Method> methods = HandlerUtil.findHandlerMethods(CommandHandler.class, Handles.class);
        HandlerInstanceAndMethod handlerInstanceAndMethod = new HandlerInstanceAndMethod(commandHandler, methods.get(0));
        assertThat(handlerInstanceAndMethod.toString(), CoreMatchers.notNullValue());
    }

    @Test
    public void shouldNotReturnNullWithNullHandlerInstance() {
        List<Method> methods = HandlerUtil.findHandlerMethods(CommandHandler.class, Handles.class);
        HandlerInstanceAndMethod handlerInstanceAndMethod = new HandlerInstanceAndMethod(null, methods.get(0));
        assertThat(handlerInstanceAndMethod.toString(), CoreMatchers.notNullValue());
    }

    @Test
    public void shouldNotReturnNullWithNullMethod() {
        assertThat(new HandlerInstanceAndMethod(commandHandler, null).toString(), CoreMatchers.notNullValue());
    }

    private Envelope testEnvelope(String fileName) throws IOException {
        JsonObjectConverter jsonObjectConverter = new JsonObjectConverter();

        String jsonString = Resources.toString(Resources.getResource("json/" + fileName), Charset.defaultCharset());
        return jsonObjectConverter.asEnvelope(jsonObjectConverter.fromString(jsonString));
    }

    public static class CommandHandler {

        @Handles("test-context.commands.create-something")
        public void handler1(final Envelope envelope) {
            if (envelope == null) throw new IllegalArgumentException();

        }

    }


}
