package uk.gov.justice.services.core.enveloper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.core.enveloper.exception.InvalidEventException;
import uk.gov.justice.services.core.extension.EventFoundEvent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.JsonObject;
import javax.json.JsonValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEnveloperTest {

    private static final String TEST_EVENT_NAME = "test.event.something-happened";
    private static final UUID COMMAND_UUID = UUID.randomUUID();
    private static final UUID OLD_CAUSATION_ID = UUID.randomUUID();
    private static final String TEST_NAME = "test.query.query-response";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private DefaultEnveloper enveloper;

    private Object object;

    @Mock
    private ObjectToJsonValueConverter objectToJsonValueConverter;

    @Mock
    private JsonEnvelope envelope;

    @Mock
    private EventFoundEvent event;

    @Mock
    private JsonObject payload;

    @Mock
    private Clock clock;

    @Before
    public void setup() throws JsonProcessingException {
        object = new TestEvent();

        doReturn(TestEvent.class).when(event).getClazz();
        when(event.getEventName()).thenReturn(TEST_EVENT_NAME);
    }

    @Test
    public void shouldEnvelopeEventObject() throws JsonProcessingException {
        enveloper.register(event);
        when(envelope.metadata()).thenReturn(
                metadataOf(COMMAND_UUID, TEST_EVENT_NAME)
                        .withCausation(OLD_CAUSATION_ID)
                        .build());
        when(objectToJsonValueConverter.convert(object)).thenReturn(payload);
        when(clock.now()).thenReturn(ZonedDateTime.now());

        JsonEnvelope event = enveloper.withMetadataFrom(envelope).apply(object);

        assertThat(event.payloadAsJsonObject(), equalTo(payload));
        assertThat(event.metadata().id(), notNullValue());
        assertThat(event.metadata().name(), equalTo(TEST_EVENT_NAME));
        assertThat(event.metadata().causation().size(), equalTo(2));
        assertThat(event.metadata().causation().get(0), equalTo(OLD_CAUSATION_ID));
        assertThat(event.metadata().causation().get(1), equalTo(COMMAND_UUID));
        verify(objectToJsonValueConverter, times(1)).convert(object);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNullEvent() throws JsonProcessingException {
        enveloper.withMetadataFrom(envelope).apply(null);
    }

    @Test
    public void shouldEnvelopeObjectWithName() throws JsonProcessingException {
        when(envelope.metadata()).thenReturn(
                metadataOf(COMMAND_UUID, TEST_NAME)
                        .withCausation(OLD_CAUSATION_ID)
                        .build());
        when(objectToJsonValueConverter.convert(object)).thenReturn(payload);
        when(clock.now()).thenReturn(ZonedDateTime.now());

        JsonEnvelope event = enveloper.withMetadataFrom(envelope, TEST_NAME).apply(object);

        assertThat(event.payload(), equalTo(payload));
        assertThat(event.metadata().id(), notNullValue());
        assertThat(event.metadata().name(), equalTo(TEST_NAME));
        assertThat(event.metadata().causation().size(), equalTo(2));
        assertThat(event.metadata().causation().get(0), equalTo(OLD_CAUSATION_ID));
        assertThat(event.metadata().causation().get(1), equalTo(COMMAND_UUID));
        verify(objectToJsonValueConverter, times(1)).convert(object);
    }

    @Test
    public void shouldEnvelopeMapNullObjectWithName() throws JsonProcessingException {
        when(envelope.metadata()).thenReturn(
                metadataOf(COMMAND_UUID, TEST_NAME)
                        .withCausation(OLD_CAUSATION_ID)
                        .build());
        when(clock.now()).thenReturn(ZonedDateTime.now());

        JsonEnvelope event = enveloper.withMetadataFrom(envelope, TEST_NAME).apply(null);

        assertThat(event.payload(), equalTo(JsonValue.NULL));
        assertThat(event.metadata().id(), notNullValue());
        assertThat(event.metadata().name(), equalTo(TEST_NAME));
        assertThat(event.metadata().causation().size(), equalTo(2));
        assertThat(event.metadata().causation().get(0), equalTo(OLD_CAUSATION_ID));
        assertThat(event.metadata().causation().get(1), equalTo(COMMAND_UUID));
        verify(objectToJsonValueConverter, times(0)).convert(object);
    }

    @Test
    public void shouldEnvelopeObjectWithoutCausation() throws JsonProcessingException {
        enveloper.register(event);
        when(envelope.metadata()).thenReturn(
                metadataOf(COMMAND_UUID, TEST_EVENT_NAME)
                        .build());
        when(objectToJsonValueConverter.convert(object)).thenReturn(payload);
        when(clock.now()).thenReturn(ZonedDateTime.now());

        JsonEnvelope event = enveloper.withMetadataFrom(envelope).apply(object);

        assertThat(event.payloadAsJsonObject(), equalTo(payload));
        assertThat(event.metadata().id(), notNullValue());
        assertThat(event.metadata().name(), equalTo(TEST_EVENT_NAME));
        assertThat(event.metadata().causation().size(), equalTo(1));
        assertThat(event.metadata().causation().get(0), equalTo(COMMAND_UUID));
        verify(objectToJsonValueConverter, times(1)).convert(object);
    }

    @Test
    public void shouldThrowExceptionIfProvidedInvalidEventObject() {
        exception.expect(InvalidEventException.class);
        exception.expectMessage("Failed to map event. No event registered for class java.lang.String");

        enveloper.withMetadataFrom(envelope).apply("InvalidEventObject");
    }

    @Event("Test-Event")
    public static class TestEvent {
    }
}
