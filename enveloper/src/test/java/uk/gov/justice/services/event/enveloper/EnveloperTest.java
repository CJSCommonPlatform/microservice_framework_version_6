package uk.gov.justice.services.event.enveloper;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Event;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.event.enveloper.exception.InvalidEventException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.CAUSATION;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.CLIENT_ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.CONTEXT;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.CORRELATION;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.SESSION_ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.STREAM;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.STREAM_ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.USER_ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataFrom;

@RunWith(MockitoJUnitRunner.class)
public class EnveloperTest {

    private static final UUID CLIENT_ID_VALUE = UUID.randomUUID();
    private static final UUID USER_ID_VALUE = UUID.randomUUID();
    private static final UUID SESSION_ID_VALUE = UUID.randomUUID();
    private static final UUID STREAM_ID_VALUE = UUID.randomUUID();
    private static final int VERSION = 5;
    private static final String TEST_COMMAND_NAME = "test.commands.do-something";
    private static final String TEST_EVENT_NAME = "test.events.something-happened";
    private static final UUID COMMAND_UUID = UUID.randomUUID();
    private static final UUID OLD_CAUSATION_ID = UUID.randomUUID();
    private static final String TEST_NAME = "test.queries.query-response";

    private Enveloper enveloper;

    private Object object;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Mock
    private Envelope envelope;

    @Mock
    private EventFoundEvent event;

    @Mock
    private JsonObject payload;

    @Before
    public void setup() throws JsonProcessingException {
        enveloper = new Enveloper();
        enveloper.objectToJsonObjectConverter = objectToJsonObjectConverter;
        object = new TestEvent();

        doReturn(TestEvent.class).when(event).getClazz();
        when(event.getEventName()).thenReturn(TEST_EVENT_NAME);


    }

    @Test
    public void shouldMapObjectToEvent() throws JsonProcessingException {
        enveloper.register(event);
        when(envelope.metadata()).thenReturn(metadata(true));
        when(objectToJsonObjectConverter.convert(object)).thenReturn(payload);

        Envelope event = enveloper.withMetadataFrom(envelope).apply(object);

        assertThat(event.payload(), equalTo(payload));
        assertThat(event.metadata().id(), notNullValue());
        assertThat(event.metadata().name(), equalTo(TEST_EVENT_NAME));
        Assert.assertThat(event.metadata().causation().size(), equalTo(2));
        Assert.assertThat(event.metadata().causation().get(0), equalTo(OLD_CAUSATION_ID));
        Assert.assertThat(event.metadata().causation().get(1), equalTo(COMMAND_UUID));
        verify(objectToJsonObjectConverter, times(1)).convert(object);
    }

    @Test
    public void shouldMapObjectToEnvelopeWithName() throws JsonProcessingException {
        when(envelope.metadata()).thenReturn(metadata(true));
        when(objectToJsonObjectConverter.convert(object)).thenReturn(payload);

        Envelope event = enveloper.withMetadataFrom(envelope, TEST_NAME).apply(object);

        assertThat(event.payload(), equalTo(payload));
        assertThat(event.metadata().id(), notNullValue());
        assertThat(event.metadata().name(), equalTo(TEST_NAME));
        Assert.assertThat(event.metadata().causation().size(), equalTo(2));
        Assert.assertThat(event.metadata().causation().get(0), equalTo(OLD_CAUSATION_ID));
        Assert.assertThat(event.metadata().causation().get(1), equalTo(COMMAND_UUID));
        verify(objectToJsonObjectConverter, times(1)).convert(object);
    }

    @Test
    public void shouldMapObjectToEventWithoutCausation() throws JsonProcessingException {
        enveloper.register(event);
        when(envelope.metadata()).thenReturn(metadata(false));
        when(objectToJsonObjectConverter.convert(object)).thenReturn(payload);

        Envelope event = enveloper.withMetadataFrom(envelope).apply(object);

        assertThat(event.payload(), equalTo(payload));
        assertThat(event.metadata().id(), notNullValue());
        assertThat(event.metadata().name(), equalTo(TEST_EVENT_NAME));
        Assert.assertThat(event.metadata().causation().size(), equalTo(1));
        Assert.assertThat(event.metadata().causation().get(0), equalTo(COMMAND_UUID));
        verify(objectToJsonObjectConverter, times(1)).convert(object);
    }

    @Test
    public void shouldThrowExceptionIfProvidedInvalidEventObject() {
        exception.expect(InvalidEventException.class);
        exception.expectMessage("Failed to map event. No event registered for class java.lang.String");

        enveloper.withMetadataFrom(envelope).apply("InvalidEventObject");
    }

    private Metadata metadata(boolean withCausation) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add(ID, COMMAND_UUID.toString())
                .add(NAME, TEST_COMMAND_NAME)
                .add(CORRELATION, Json.createObjectBuilder()
                        .add(CLIENT_ID, CLIENT_ID_VALUE.toString())
                )
                .add(CONTEXT, Json.createObjectBuilder()
                        .add(USER_ID, USER_ID_VALUE.toString())
                        .add(SESSION_ID, SESSION_ID_VALUE.toString())
                )
                .add(STREAM, Json.createObjectBuilder()
                        .add(STREAM_ID, STREAM_ID_VALUE.toString())
                        .add(JsonObjectMetadata.VERSION, VERSION)
                );
        if (withCausation) {
            builder.add(CAUSATION, Json.createArrayBuilder()
                    .add(OLD_CAUSATION_ID.toString())
            );
        }

        return metadataFrom(builder.build());
    }

    @Event("Test-Event")
    public static class TestEvent {
    }
}
