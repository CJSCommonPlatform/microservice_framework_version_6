package uk.gov.justice.services.example.cakeshop.command.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher.isHandler;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MakeCakeCommandHandlerTest {

    private static final String EVENT_NAME = "example.event.cake-made";
    private static final UUID CAKE_ID = UUID.randomUUID();

    @Mock
    JsonEnvelope envelope;

    @Mock
    EventSource eventSource;

    @Mock
    EventStream eventStream;

    @InjectMocks
    private MakeCakeCommandHandler makeCakeCommandHandler;

    @Test
    public void shouldHaveCorrectHandlesAnnotation() throws Exception {
        assertThat(makeCakeCommandHandler, isHandler(COMMAND_HANDLER)
                .with(method("makeCake").thatHandles("example.make-cake")));
    }

    @Test
    public void shouldHandleMakeCakeCommand() throws Exception {
        makeCakeCommandHandler.makeCake(
                envelope()
                        .with(metadataWithRandomUUID(EVENT_NAME))
                        .withPayloadOf(CAKE_ID.toString(), "cakeId")
                        .build());
    }


}
