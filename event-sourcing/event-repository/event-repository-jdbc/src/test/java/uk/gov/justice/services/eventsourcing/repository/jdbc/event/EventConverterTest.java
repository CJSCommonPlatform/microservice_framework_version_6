package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidStreamIdException;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.common.helper.StoppedClock;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;

@RunWith(MockitoJUnitRunner.class)
public class EventConverterTest {

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
    private final Clock clock = new StoppedClock(new UtcClock().now());
    private EventConverter eventConverter;


    @Before
    public void setup() {
        eventConverter = new EventConverter();
        eventConverter.stringToJsonObjectConverter = new StringToJsonObjectConverter();
        eventConverter.jsonObjectEnvelopeConverter = new DefaultJsonObjectEnvelopeConverter();
    }

    @Test
    public void shouldCreateEventLog() throws Exception {
        JsonEnvelope envelope = envelopeFrom(
                metadataBuilder()
                        .withId(ID)
                        .withName(NAME)
                        .withStreamId(STREAM_ID)
                        .withVersion(SEQUENCE_ID)
                        .createdAt(clock.now()),
                createObjectBuilder().add(PAYLOAD_FIELD_NAME, PAYLOAD_FIELD_VALUE));
        Event event = eventConverter.eventOf(envelope);

        assertThat(event.getId(), equalTo(ID));
        assertThat(event.getName(), equalTo(NAME));
        assertThat(event.getStreamId(), equalTo(STREAM_ID));
        assertThat(event.getSequenceId(), equalTo(SEQUENCE_ID));
        assertThat(event.getCreatedAt(), is(clock.now()));
        JSONAssert.assertEquals(METADATA_JSON, event.getMetadata(), false);
        JSONAssert.assertEquals(envelope.payloadAsJsonObject().toString(), event.getPayload(), false);
    }

    @Test(expected = InvalidStreamIdException.class)
    public void shouldThrowExceptionOnNullStreamId() throws Exception {
        eventConverter.eventOf(envelopeFrom(metadataBuilder().withId(ID).withName(NAME), createObjectBuilder()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnMissingCreatedAt() throws Exception {
        eventConverter.eventOf((envelopeFrom(metadataBuilder().withId(ID).withName(NAME).withStreamId(STREAM_ID), createObjectBuilder())));
    }

    @Test
    public void shouldCreateEnvelope() throws Exception {
        JsonEnvelope actualEnvelope = eventConverter.envelopeOf(new Event(ID, STREAM_ID, SEQUENCE_ID, NAME, METADATA_JSON, PAYLOAD_JSON, new UtcClock().now()));

        assertThat(actualEnvelope.metadata().id(), equalTo(ID));
        assertThat(actualEnvelope.metadata().name(), equalTo(NAME));
        String actualPayload = actualEnvelope.payloadAsJsonObject().toString();
        JSONAssert.assertEquals(PAYLOAD_JSON, actualPayload, false);
    }


}
