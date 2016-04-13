package uk.gov.justice.services.example.cakeshop.command.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.event.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

@RunWith(MockitoJUnitRunner.class)
public class AddRecipeCommandHandlerTest {

    private static final String EVENT_NAME = "cakeshop.events.recipe-added";
    private static final UUID RECIPE_ID = UUID.randomUUID();
    private static final String RECIPE_NAME = "Test Recipe";

    @Mock
    JsonEnvelope jsonEnvelope;

    @Mock
    EventSource eventSource;

    @Mock
    Enveloper enveloper;

    @Mock
    Function<Object, JsonEnvelope> enveloperFunction;

    @Mock
    Stream<RecipeAdded> events;

    @Mock
    Stream<JsonEnvelope> envelopes;

    @InjectMocks
    private AddRecipeCommandHandler addRecipeCommandHandler;

    @Test
    public void shouldHandleAddRecipeCommand() throws Exception {
        final JsonEnvelope command = createCommand();
        final EventStreamStub eventStreamStub = new EventStreamStub();

        when(enveloper.withMetadataFrom(command)).thenReturn(enveloperFunction);
        when(enveloperFunction.apply(anyObject())).thenReturn(jsonEnvelope);
        when(eventSource.getStreamById(RECIPE_ID)).thenReturn(eventStreamStub);

        addRecipeCommandHandler.addRecipe(command);

        assertThat(eventStreamStub.events, notNullValue());
        assertThat(eventStreamStub.events.findFirst().get(), equalTo(jsonEnvelope));

    }

    private JsonEnvelope createCommand() {
        final JsonObject metadataAsJsonObject = Json.createObjectBuilder()
                .add(ID, UUID.randomUUID().toString())
                .add(NAME, EVENT_NAME)
                .build();

        final JsonObject payloadAsJsonObject = Json.createObjectBuilder()
                .add("recipeId", RECIPE_ID.toString())
                .add("name", RECIPE_NAME)
                .add("ingredients", Json.createArrayBuilder().build())
                .build();

        return DefaultJsonEnvelope.envelopeFrom(metadataFrom(metadataAsJsonObject), payloadAsJsonObject);
    }

    private class EventStreamStub implements EventStream {

        private Stream<JsonEnvelope> events;

        @Override
        public Stream<JsonEnvelope> read() {
            return null;
        }

        @Override
        public Stream<JsonEnvelope> readFrom(Long version) {
            return null;
        }

        @Override
        public void append(Stream<JsonEnvelope> events) throws EventStreamException {
            this.events = events;
        }

        @Override
        public void appendAfter(Stream<JsonEnvelope> events, Long version) throws EventStreamException {

        }

        @Override
        public Long getCurrentVersion() {
            return null;
        }

        @Override
        public UUID getId() {
            return null;
        }
    }

}