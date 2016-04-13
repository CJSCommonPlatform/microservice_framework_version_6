package uk.gov.justice.services.core.dispatcher;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherTest {

    private static final String NAME = "test.command.do-something";

    @Mock
    private JsonEnvelope jsonEnvelope;

    @Mock
    private Metadata metadata;

    @Before
    public void setup() {
        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(NAME);
    }

    @Test
    public void shouldDispatchToAValidHandler() throws Exception {
        Dispatcher dispatcher = new Dispatcher();
        TestHandler testHandler = new TestHandler();
        dispatcher.register(testHandler);
        dispatcher.asynchronousDispatch(jsonEnvelope);
        assertThat(testHandler.jsonEnvelope, equalTo(jsonEnvelope));
    }

    @Test(expected = MissingHandlerException.class)
    public void shouldThrowExceptionWithNoHandler() throws Exception {
        new Dispatcher().asynchronousDispatch(jsonEnvelope);
    }

    @ServiceComponent(COMMAND_API)
    public static class TestHandler {

        JsonEnvelope jsonEnvelope;

        @Handles(NAME)
        public void handle(JsonEnvelope jsonEnvelope) {
            this.jsonEnvelope = jsonEnvelope;
        }

    }
}
