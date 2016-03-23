package uk.gov.justice.services.example.cakeshop.command.handler;

import org.junit.Test;
import uk.gov.justice.services.messaging.DefaultEnvelope;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

public class TemporaryEventUtilTest {

    private final static UUID COMMAND_ID = UUID.randomUUID();
    private final static String COMMAND_NAME = "cakeshop.commands.add-recipe";
    private final static String EVENT_NAME = "cakeshop.events.recipe-added";

    @Test
    public void shouldReturnStreamOfEvents() {
        Envelope command = createTestCommand();

        Stream<Envelope> actual = new TemporaryEventUtil().eventsFrom(command);

        Envelope actualEvent = actual.findFirst().get();
        assertThat(actualEvent.metadata().id(), not(equalTo(COMMAND_ID)));
        assertThat(actualEvent.metadata().name(), equalTo(EVENT_NAME));
        assertThat(actualEvent.payload(), equalTo(command.payload()));

    }

    private Envelope createTestCommand() {
        final JsonObject metadataAsJsonObject = Json.createObjectBuilder()
                .add(ID, COMMAND_ID.toString())
                .add(NAME, EVENT_NAME)
                .build();
        final Metadata metadata = metadataFrom(metadataAsJsonObject);

        final JsonObject payload = Json.createObjectBuilder()
                .build();

        return DefaultEnvelope.envelopeFrom(metadata, payload);
    }
}