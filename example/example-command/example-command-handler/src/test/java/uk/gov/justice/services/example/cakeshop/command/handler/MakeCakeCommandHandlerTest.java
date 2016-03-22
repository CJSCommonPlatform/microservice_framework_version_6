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

import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

@RunWith(MockitoJUnitRunner.class)
public class MakeCakeCommandHandlerTest {

    private static final String EVENT_NAME = "cakeshop.events.cake-made";
    private static final UUID RECIPE_ID = UUID.randomUUID();

    @Mock
    Envelope envelope;

    @Mock
    EventSource eventSource;

    @Mock
    EventStream eventStream;

    @InjectMocks
    private MakeCakeCommandHandler makeCakeCommandHandler;

    @Test
    public void shouldHandleMakeCakeCommand() throws Exception {
        when(eventSource.getStreamById(RECIPE_ID)).thenReturn(eventStream);

        Envelope envelope = createCommandEnvelope();

        makeCakeCommandHandler.handle(envelope);


    }

    private Envelope createCommandEnvelope() {
        JsonObject metadataAsJsonObject = Json.createObjectBuilder()
                .add(ID, RECIPE_ID.toString())
                .add(NAME, EVENT_NAME)
                .build();

        JsonObject payloadAsJsonObject = Json.createObjectBuilder()
                .add("recipeId", UUID.randomUUID().toString())
                .build();

        return DefaultEnvelope.envelopeFrom(metadataFrom(metadataAsJsonObject), payloadAsJsonObject);

    }

}