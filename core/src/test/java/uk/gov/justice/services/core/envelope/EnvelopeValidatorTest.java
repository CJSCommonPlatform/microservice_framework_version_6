package uk.gov.justice.services.core.envelope;


import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.core.json.SchemaLoadingException;
import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.NameToMediaTypeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.JsonValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnvelopeValidatorTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JsonSchemaValidator jsonSchemaValidator;

    @Mock
    private EnvelopeValidationExceptionHandler envelopeValidationExceptionHandler;

    @Mock
    private NameToMediaTypeConverter nameToMediaTypeConverter;

    private EnvelopeValidator envelopeValidator;

    @Before
    public void setUp() throws Exception {
        envelopeValidator = new EnvelopeValidator(jsonSchemaValidator, envelopeValidationExceptionHandler, nameToMediaTypeConverter, objectMapper);
    }

    @Test
    public void shouldValidatePayloadAgainstJsonSchema() throws Exception {
        final String metadataName = "some-name";
        final MediaType mediaType = mock(MediaType.class);

        JsonEnvelope envelope = envelope().with(metadataWithRandomUUID(metadataName)).withPayloadOf("valueABC", "someElement").build();

        final String jsonStringRepresentation = "dummyJsonStringRepresentation";

        when(nameToMediaTypeConverter.convert(metadataName)).thenReturn(mediaType);
        when(objectMapper.writeValueAsString(envelope.payload())).thenReturn(jsonStringRepresentation);

        envelopeValidator.validate(envelope);

        verify(jsonSchemaValidator).validate(jsonStringRepresentation, mediaType);

    }

    @Test
    public void shouldHandleJsonSerialisationException() throws Exception {
        final JsonProcessingException jsonProcessingException = new JsonProcessingException("") {
        };
        when(objectMapper.writeValueAsString(any())).thenThrow(jsonProcessingException);

        envelopeValidator.validate(envelope().with(metadataWithDefaults()).build());

        final EnvelopeValidationException exception = handledException();
        assertThat(exception.getCause(), is(jsonProcessingException));
        assertThat(exception.getMessage(), is("Error serialising json."));
    }

    @Test
    public void shouldHandleJsonValidationException() {
        final ValidationException jsonValidationException = new ValidationException(null, Object.class, null);
        doThrow(jsonValidationException).when(jsonSchemaValidator).validate(anyString(), any());
        envelopeValidator.validate(envelope().with(metadataWithRandomUUID("msgNameABC")).withPayloadOf("SensitiveData", "property1").build());

        final EnvelopeValidationException exception = handledException();
        assertThat(exception.getCause(), is(jsonValidationException));
        assertThat(exception.getMessage(), allOf(containsString("Message not valid against schema"),
                containsString("msgNameABC"), containsString("property1"), not(containsString("SensitiveData"))));
    }

    @Test
    public void shouldHandleSchemaLoadingException() {
        final SchemaLoadingException schemaLoadingException = new SchemaLoadingException("Schema does not exists");
        doThrow(schemaLoadingException).when(jsonSchemaValidator).validate(anyString(), any());
        envelopeValidator.validate(envelope().with(metadataWithRandomUUID("BCD")).build());

        final EnvelopeValidationException exception = handledException();
        assertThat(exception.getCause(), is(schemaLoadingException));
        assertThat(exception.getMessage(), is("Could not load json schema that matches message type BCD."));
    }

    @Test
    public void shouldHandleExceptionIfNoMetadataInEnvelope() throws Exception {

        envelopeValidator.validate(envelope().build());

        final EnvelopeValidationException exception = handledException();

        assertThat(exception.getMessage(), is("Metadata not set in the envelope."));


    }

    @Test
    public void shouldSkipValidationIfPayloadNULL() throws Exception {

        envelopeValidator.validate(envelopeFrom(metadataWithRandomUUID("some-name"), JsonValue.NULL));

        verifyZeroInteractions(jsonSchemaValidator);

    }


    private EnvelopeValidationException handledException() {
        ArgumentCaptor<EnvelopeValidationException> exceptionCaptor = ArgumentCaptor.forClass(EnvelopeValidationException.class);
        verify(envelopeValidationExceptionHandler).handle(exceptionCaptor.capture());
        return exceptionCaptor.getValue();
    }
}
