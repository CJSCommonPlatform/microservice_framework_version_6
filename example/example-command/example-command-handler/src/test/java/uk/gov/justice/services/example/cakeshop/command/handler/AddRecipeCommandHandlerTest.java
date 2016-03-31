package uk.gov.justice.services.example.cakeshop.command.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.DefaultEnvelope;
import uk.gov.justice.services.messaging.Envelope;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

@RunWith(MockitoJUnitRunner.class)
public class AddRecipeCommandHandlerTest {

    private static final String EVENT_NAME = "cakeshop.events.recipe-added";
    private static final UUID RECIPE_ID = UUID.randomUUID();

    @Mock
    Envelope envelope;

    @Mock
    EventSource eventSource;

    @Mock
    EventStream eventStream;

    @Mock
    TemporaryEventUtil temporaryEventUtil;

    @Mock
    Stream<Envelope> events;

    @Mock
    private Sender sender;


    @InjectMocks
    private AddRecipeCommandHandler addRecipeCommandHandler;

    @Test
    public void shouldHandleMakeCakeCommand() throws Exception {
        final Envelope command = createCommand();
        when(eventSource.getStreamById(RECIPE_ID)).thenReturn(eventStream);
        when(temporaryEventUtil.eventsFrom(command, EVENT_NAME)).thenReturn(events);

        addRecipeCommandHandler.addRecipe(command);

        verify(eventStream, times(1)).append(temporaryEventUtil.eventsFrom(command, EVENT_NAME));
    }

    private Envelope createCommand() {
        final JsonObject metadataAsJsonObject = Json.createObjectBuilder()
                .add(ID, UUID.randomUUID().toString())
                .add(NAME, EVENT_NAME)
                .build();

        final JsonObject payloadAsJsonObject = Json.createObjectBuilder()
                .add("recipeId", RECIPE_ID.toString())
                .build();

        return DefaultEnvelope.envelopeFrom(metadataFrom(metadataAsJsonObject), payloadAsJsonObject);
    }

}