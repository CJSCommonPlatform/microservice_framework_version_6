package uk.gov.justice.services.core.dispatcher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.handler.HandlerInstanceAndMethod;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

@RunWith(MockitoJUnitRunner.class)
public class AsynchronousDispatcherTest {

    private static final String NAME = "test.commands.do-something";

    @Mock
    private Envelope envelope;

    @Mock
    private HandlerInstanceAndMethod handlerInstanceAndMethod;

    @Mock
    private Metadata metadata;

    @Before
    public void setup() {
        when(envelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(NAME);
    }

    @Test
    public void shouldDispatchToAValidHandler() throws Exception {
        AsynchronousDispatcher dispatcher = new AsynchronousDispatcher();
        TestHandler testHandler = new TestHandler();
        dispatcher.register(testHandler);
        dispatcher.dispatch(envelope);
        assertThat(testHandler.envelope, equalTo(envelope));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWithNoHandler() throws Exception {
        new AsynchronousDispatcher().dispatch(envelope);
    }

    @ServiceComponent(COMMAND_API)
    public static class TestHandler {

        Envelope envelope;

        @Handles(NAME)
        public void handle(Envelope envelope) {
            this.envelope = envelope;
        }

    }

}