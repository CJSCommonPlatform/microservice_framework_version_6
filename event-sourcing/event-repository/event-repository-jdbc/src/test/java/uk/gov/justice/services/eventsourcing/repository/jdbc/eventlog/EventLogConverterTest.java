package uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.justice.services.common.converter.JsonObjectToStringConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidStreamIdException;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.Metadata;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

@RunWith(MockitoJUnitRunner.class)
public class EventLogConverterTest {

    private final static String PAYLOAD_FIELD_NAME = "field";
    private final static String PAYLOAD_FIELD_VALUE = "Value";

    private final static UUID ID = UUID.randomUUID();
    private final static UUID STREAM_ID = UUID.randomUUID();
    private final static Long SEQUENCE_ID = 5L;
    private final static String NAME = "test.event.did-something";
    private final static String METADATA_JSON = "{\"id\": \"" + ID.toString() + "\", " +
            "\"name\": \"" + NAME + "\"" +
            "}";
    private final static String PAYLOAD_JSON = "{\"" + PAYLOAD_FIELD_NAME + "\": \"" + PAYLOAD_FIELD_VALUE + "\"}";

    private EventLogConverter eventLogConverter;

    @Before
    public void setup() {
        eventLogConverter = new EventLogConverter();
        eventLogConverter.jsonObjectToStringConverter = new JsonObjectToStringConverter();
        eventLogConverter.stringToJsonObjectConverter = new StringToJsonObjectConverter();
        eventLogConverter.jsonObjectEnvelopeConverter = new JsonObjectEnvelopeConverter();
    }

    @Test
    public void shouldCreateEventLog() throws Exception {
        JsonEnvelope expectedJsonEnvelope = createTestEnvelope();
        String expectedPayloadAsJsonString = new JsonObjectToStringConverter().convert(expectedJsonEnvelope.payloadAsJsonObject());
        EventLog eventLog = eventLogConverter.createEventLog(expectedJsonEnvelope, STREAM_ID, SEQUENCE_ID);

        assertThat(eventLog.getId(), equalTo(ID));
        assertThat(eventLog.getName(), equalTo(NAME));
        assertThat(eventLog.getStreamId(), equalTo(STREAM_ID));
        assertThat(eventLog.getSequenceId(), equalTo(SEQUENCE_ID));
        JSONAssert.assertEquals(METADATA_JSON, eventLog.getMetadata(), false);
        JSONAssert.assertEquals(expectedPayloadAsJsonString, eventLog.getPayload(), false);
    }

    @Test(expected = InvalidStreamIdException.class)
    public void shouldThrowExceptionOnNullStreamId() throws Exception {
        eventLogConverter.createEventLog(createTestEnvelope(), null, SEQUENCE_ID);
    }

    @Test
    public void shouldCreateEnvelope() throws Exception {
        JsonEnvelope actualJsonEnvelope = eventLogConverter.createEnvelope(createEventLog());

        assertThat(actualJsonEnvelope.metadata().id(), equalTo(ID));
        assertThat(actualJsonEnvelope.metadata().name(), equalTo(NAME));
        String actualPayload = new JsonObjectToStringConverter().convert(actualJsonEnvelope.payloadAsJsonObject());
        JSONAssert.assertEquals(PAYLOAD_JSON, actualPayload, false);
    }

    private EventLog createEventLog() {
        return new EventLog(ID, STREAM_ID, SEQUENCE_ID, NAME, METADATA_JSON, PAYLOAD_JSON);
    }

    private JsonEnvelope createTestEnvelope() throws IOException {
        final Metadata metadata = metadataFrom(Json.createObjectBuilder()
                .add("id", ID.toString())
                .add("name", NAME)
                .build());

        final JsonObject payload = Json.createObjectBuilder().add(PAYLOAD_FIELD_NAME, PAYLOAD_FIELD_VALUE).build();

        return DefaultJsonEnvelope.envelopeFrom(metadata, payload);
    }

}