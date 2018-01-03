package uk.gov.justice.services.core.envelope;

import static java.lang.String.format;
import static javax.json.JsonValue.NULL;

import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.core.json.SchemaLoadingException;
import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;

import javax.json.JsonValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.ValidationException;

public class EnvelopeValidator {

    private final JsonSchemaValidator jsonSchemaValidator;
    private final ObjectMapper objectMapper;
    private final EnvelopeValidationExceptionHandler envelopeValidationExceptionHandler;

    public EnvelopeValidator(
            final JsonSchemaValidator jsonSchemaValidator,
            final ObjectMapper objectMapper,
            final EnvelopeValidationExceptionHandler envelopeValidationExceptionHandler) {
        this.jsonSchemaValidator = jsonSchemaValidator;
        this.objectMapper = objectMapper;
        this.envelopeValidationExceptionHandler = envelopeValidationExceptionHandler;
    }

    public void validate(final JsonEnvelope jsonEnvelope, final String actionName, final Optional<MediaType> mediaType) {
        try {
            final JsonValue payload = jsonEnvelope.payload();
            if (!NULL.equals(payload)) {

                jsonSchemaValidator.validate(
                        objectMapper.writeValueAsString(payload),
                        actionName,
                        mediaType);
            }
        } catch (final JsonProcessingException e) {
            envelopeValidationExceptionHandler.handle(new EnvelopeValidationException("Error serialising json.", e));
        } catch (final SchemaLoadingException e) {
            envelopeValidationExceptionHandler.handle(new EnvelopeValidationException(
                    format("Could not load json schema that matches message type %s.", actionName), e));
        } catch (final ValidationException e) {
            envelopeValidationExceptionHandler.handle(new EnvelopeValidationException(
                    format("Message not valid against schema: \n%s", jsonEnvelope.toObfuscatedDebugString()), e));
        } catch (final EnvelopeValidationException e) {
            envelopeValidationExceptionHandler.handle(e);
        }
    }
}
