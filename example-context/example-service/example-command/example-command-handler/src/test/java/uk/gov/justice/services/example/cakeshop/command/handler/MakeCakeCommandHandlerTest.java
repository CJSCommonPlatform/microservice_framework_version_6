package uk.gov.justice.services.example.cakeshop.command.handler;

import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MakeCakeCommandHandlerTest {

    private static final String EVENT_NAME = "cakeshop.event.cake-made";
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
    public void shouldHandleMakeCakeCommand() throws Exception {
        when(eventSource.getStreamById(CAKE_ID)).thenReturn(eventStream);

        JsonEnvelope envelope = createCommandEnvelope();

        makeCakeCommandHandler.handle(envelope);

    }

    private JsonEnvelope createCommandEnvelope() {
        JsonObject metadataAsJsonObject = Json.createObjectBuilder()
                .add(ID, UUID.randomUUID().toString())
                .add(NAME, EVENT_NAME)
                .build();

        JsonObject payloadAsJsonObject = Json.createObjectBuilder()
                .add("cakeId", CAKE_ID.toString())
                .build();

        return DefaultJsonEnvelope.envelopeFrom(metadataFrom(metadataAsJsonObject), payloadAsJsonObject);

    }

}
