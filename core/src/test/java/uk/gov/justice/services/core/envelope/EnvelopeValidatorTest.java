package uk.gov.justice.services.core.envelope;

import static java.util.Optional.of;
import static javax.json.JsonValue.NULL;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.json.JsonSchemaValidationException;
import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.core.json.SchemaLoadingException;
import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.json.JsonValue;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.ValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnvelopeValidatorTest {

    @Mock
    private JsonSchemaValidator jsonSchemaValidator;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EnvelopeValidationExceptionHandler envelopeValidationExceptionHandler;

    @Captor
    private ArgumentCaptor<EnvelopeValidationException> exceptionArgumentCaptor;


    @InjectMocks
    private EnvelopeValidator envelopeValidator;

    @Test
    public void shouldValidateThePayloadOfAJsonEnvelope() throws Exception {

        final String actionName = "example.action-name";
        final Optional<MediaType> mediaType = of(new MediaType("application/vnd.example.action-name+json"));
        final String payloadJson = "{\"some\": \"json\"}";

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final JsonValue payload = mock(JsonValue.class);

        when(jsonEnvelope.payload()).thenReturn(payload);
        when(objectMapper.writeValueAsString(payload)).thenReturn(payloadJson);

        envelopeValidator.validate(jsonEnvelope, actionName, mediaType);

        verify(jsonSchemaValidator).validate(
                payloadJson,
                actionName,
                mediaType);
    }

    @Test
    public void shouldDoNothingIfTheEnvelopePayloadIsNull() throws Exception {

        final String actionName = "example.action-name";
        final Optional<MediaType> mediaType = of(new MediaType("application/vnd.example.action-name+json"));

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        when(jsonEnvelope.payload()).thenReturn(NULL);

        envelopeValidator.validate(jsonEnvelope, actionName, mediaType);

        verifyZeroInteractions(objectMapper);
        verifyZeroInteractions(jsonSchemaValidator);
    }

    @Test
    public void shouldHandleAJsonProcessingException() throws Exception {

        final JsonProcessingException jsonProcessingException = new JsonGenerationException("Ooops");

        final String actionName = "example.action-name";
        final Optional<MediaType> mediaType = of(new MediaType("application/vnd.example.action-name+json"));

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final JsonValue payload = mock(JsonValue.class);

        when(jsonEnvelope.payload()).thenReturn(payload);
        when(objectMapper.writeValueAsString(payload)).thenThrow(jsonProcessingException);

        envelopeValidator.validate(jsonEnvelope, actionName, mediaType);

        verify(envelopeValidationExceptionHandler).handle(exceptionArgumentCaptor.capture());

        final EnvelopeValidationException envelopeValidationException = exceptionArgumentCaptor.getValue();

        assertThat(envelopeValidationException.getMessage(), is("Error serialising json."));
        assertThat(envelopeValidationException.getCause(), is(jsonProcessingException));

        verifyZeroInteractions(jsonSchemaValidator);
    }

    @Test
    public void shouldHandleASchemaLoadingException() throws Exception {

        final SchemaLoadingException schemaLoadingException = new SchemaLoadingException("Ooops");

        final String actionName = "exaple.action-name";
        final String payloadJson = "{\"some\": \"json\"}";
        final Optional<MediaType> mediaType = of(new MediaType("application/vnd.example.action-name+json"));

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final JsonValue payload = mock(JsonValue.class);

        when(jsonEnvelope.payload()).thenReturn(payload);
        when(objectMapper.writeValueAsString(payload)).thenReturn(payloadJson);
        doThrow(schemaLoadingException).when(jsonSchemaValidator).validate(
                payloadJson,
                actionName,
                mediaType);

        envelopeValidator.validate(jsonEnvelope, actionName, mediaType);

        verify(envelopeValidationExceptionHandler).handle(exceptionArgumentCaptor.capture());

        final EnvelopeValidationException envelopeValidationException = exceptionArgumentCaptor.getValue();

        assertThat(envelopeValidationException.getMessage(), is("Could not load json schema that matches message type exaple.action-name."));
        assertThat(envelopeValidationException.getCause(), is(schemaLoadingException));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void shouldHandleAValidationException() throws Exception {

        final JsonSchemaValidationException validationException = new JsonSchemaValidationException("Ooops", new ValidationException("Opps"));

        final String actionName = "exaple.action-name";
        final String payloadJson = "{\"some\": \"json\"}";
        final Optional<MediaType> mediaType = of(new MediaType("application/vnd.example.action-name+json"));

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final JsonValue payload = mock(JsonValue.class);

        when(jsonEnvelope.payload()).thenReturn(payload);
        when(objectMapper.writeValueAsString(payload)).thenReturn(payloadJson);
        when(jsonEnvelope.toObfuscatedDebugString()).thenReturn("debug-json");

        doThrow(validationException).when(jsonSchemaValidator).validate(
                payloadJson,
                actionName,
                mediaType);

        envelopeValidator.validate(jsonEnvelope, actionName, mediaType);

        verify(envelopeValidationExceptionHandler).handle(exceptionArgumentCaptor.capture());

        final EnvelopeValidationException envelopeValidationException = exceptionArgumentCaptor.getValue();

        final String exceptionMessage = "Message not valid against schema: \ndebug-json : validation error:" +
                " {\"message\":\"#: Opps\",\"violation\":\"#\",\"causingExceptions\":[]}";
        assertThat(envelopeValidationException.getMessage(), is(exceptionMessage));
        assertThat(envelopeValidationException.getCause(), is(validationException));
    }

    @Test
    public void shouldHandleAEnvelopeValidationException() throws Exception {

        final EnvelopeValidationException envelopeValidationException = new EnvelopeValidationException("Ooops");

        final String actionName = "exaple.action-name";
        final String payloadJson = "{\"some\": \"json\"}";
        final Optional<MediaType> mediaType = of(new MediaType("application/vnd.example.action-name+json"));

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final JsonValue payload = mock(JsonValue.class);

        when(jsonEnvelope.payload()).thenReturn(payload);
        when(objectMapper.writeValueAsString(payload)).thenReturn(payloadJson);
        when(jsonEnvelope.toObfuscatedDebugString()).thenReturn("debug-json");

        doThrow(envelopeValidationException).when(jsonSchemaValidator).validate(
                payloadJson,
                actionName,
                mediaType);

        envelopeValidator.validate(jsonEnvelope, actionName, mediaType);

        verify(envelopeValidationExceptionHandler).handle(exceptionArgumentCaptor.capture());

        assertThat(exceptionArgumentCaptor.getValue(), is(envelopeValidationException));
    }
}
