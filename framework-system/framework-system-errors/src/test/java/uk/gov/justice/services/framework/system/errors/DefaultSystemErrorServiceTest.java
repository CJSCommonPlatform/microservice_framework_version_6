package uk.gov.justice.services.framework.system.errors;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.framework.utilities.exceptions.StackTraceProvider;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.system.domain.EventError;
import uk.gov.justice.services.system.persistence.EventErrorLogRepository;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.JsonObject;
import javax.json.JsonValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSystemErrorServiceTest {

    private static final String AN_EMPTY_STRING = "";

    @Mock
    private EventErrorLogRepository eventErrorLogRepository;

    @Mock
    private StackTraceProvider stackTraceProvider;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private DefaultSystemErrorService defaultSystemErrorService;

    @Captor
    private ArgumentCaptor<EventError> eventErrorCaptor;

    @Test
    public void shouldCreateEventErrorAndPersist() throws Exception {

        final String messageId = "message id";
        final String componentName = "EVENT_LISTENER";
        final String eventName = "context.events.an.event";
        final String errorMessage = "Help help we're all going to die";
        final Throwable exception = new NullPointerException(errorMessage);
        final Long eventNumber = 239874L;
        final UUID eventId = randomUUID();
        final String metadataJson = "{metadata: stuff}";
        final String payloadJson = "{payload: stuff}";
        final String stackTrace = "the stacktrace";
        final ZonedDateTime erroredAt = new UtcClock().now();

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);
        final JsonObject metadataJsonObject = mock(JsonObject.class);
        final JsonValue payload = mock(JsonValue.class);

        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.id()).thenReturn(eventId);
        when(metadata.name()).thenReturn(eventName);
        when(metadata.eventNumber()).thenReturn(of(eventNumber));

        when(metadata.asJsonObject()).thenReturn(metadataJsonObject);
        when(jsonEnvelope.payload()).thenReturn(payload);

        when(metadataJsonObject.toString()).thenReturn(metadataJson);
        when(payload.toString()).thenReturn(payloadJson);
        when(stackTraceProvider.getStackTrace(exception)).thenReturn(stackTrace);

        when(clock.now()).thenReturn(erroredAt);

        defaultSystemErrorService.reportError(
                messageId,
                componentName,
                jsonEnvelope,
                exception
        );

        verify(eventErrorLogRepository).save(eventErrorCaptor.capture());

        final EventError eventError = eventErrorCaptor.getValue();

        assertThat(eventError.getMessageId(), is(messageId));
        assertThat(eventError.getComponent(), is(componentName));
        assertThat(eventError.getEventId(), is(eventId));
        assertThat(eventError.getEventName(), is(eventName));
        assertThat(eventError.getEventNumber(), is(eventNumber));
        assertThat(eventError.getMetadata(), is(metadataJson));
        assertThat(eventError.getPayload(), is(payloadJson));
        assertThat(eventError.getErrorMessage(), is(errorMessage));
        assertThat(eventError.getStacktrace(), is(stackTrace));
        assertThat(eventError.getErroredAt(), is(erroredAt));
        assertThat(eventError.getComments(), is(AN_EMPTY_STRING));
    }

    @Test
    public void shouldHandleMissingEventNumber() throws Exception {

        final String messageId = "message id";
        final String componentName = "EVENT_LISTENER";
        final String eventName = "context.events.an.event";
        final String errorMessage = "Help help we're all going to die";
        final Throwable exception = new NullPointerException(errorMessage);
        final UUID eventId = randomUUID();
        final String metadataJson = "{metadata: stuff}";
        final String payloadJson = "{payload: stuff}";
        final String stackTrace = "the stacktrace";
        final ZonedDateTime erroredAt = new UtcClock().now();

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final Metadata metadata = mock(Metadata.class);
        final JsonObject metadataJsonObject = mock(JsonObject.class);
        final JsonValue payload = mock(JsonValue.class);

        when(jsonEnvelope.metadata()).thenReturn(metadata);
        when(metadata.id()).thenReturn(eventId);
        when(metadata.name()).thenReturn(eventName);
        when(metadata.eventNumber()).thenReturn(empty());

        when(metadata.asJsonObject()).thenReturn(metadataJsonObject);
        when(jsonEnvelope.payload()).thenReturn(payload);

        when(metadataJsonObject.toString()).thenReturn(metadataJson);
        when(payload.toString()).thenReturn(payloadJson);
        when(stackTraceProvider.getStackTrace(exception)).thenReturn(stackTrace);

        when(clock.now()).thenReturn(erroredAt);

        defaultSystemErrorService.reportError(
                messageId,
                componentName,
                jsonEnvelope,
                exception
        );

        verify(eventErrorLogRepository).save(eventErrorCaptor.capture());

        final EventError eventError = eventErrorCaptor.getValue();

        assertThat(eventError.getMessageId(), is(messageId));
        assertThat(eventError.getComponent(), is(componentName));
        assertThat(eventError.getEventName(), is(eventName));
        assertThat(eventError.getErrorMessage(), is(errorMessage));
        assertThat(eventError.getEventNumber(), is(-1L));
        assertThat(eventError.getEventId(), is(eventId));
        assertThat(eventError.getMetadata(), is(metadataJson));
        assertThat(eventError.getPayload(), is(payloadJson));
        assertThat(eventError.getStacktrace(), is(stackTrace));
        assertThat(eventError.getErroredAt(), is(erroredAt));
        assertThat(eventError.getComments(), is("Event number is missing from event. Setting to -1 instead"));
    }
}
