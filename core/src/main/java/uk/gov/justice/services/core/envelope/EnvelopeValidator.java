package uk.gov.justice.services.core.envelope;


import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.ValidationException;

public class EnvelopeValidator {

    private final ObjectMapper objectMapper;
    private final JsonSchemaValidator jsonSchemaValidator;
    private final EnvelopeValidationExceptionHandler envelopeValidationExceptionHandler;

    public EnvelopeValidator(final JsonSchemaValidator jsonSchemaValidator,
                             final EnvelopeValidationExceptionHandler jsonValidationExceptionHandler,
                             final ObjectMapper objectMapper) {
        this.jsonSchemaValidator = jsonSchemaValidator;
        this.envelopeValidationExceptionHandler = jsonValidationExceptionHandler;
        this.objectMapper = objectMapper;
    }

    public void validate(final JsonEnvelope envelope) {
        try {
            jsonSchemaValidator.validate(
                    objectMapper.writeValueAsString(envelope.payload()), metadataOf(envelope).name());
        } catch (final JsonProcessingException e) {
            handle(e, "Error serialising json.");
        } catch (final ValidationException e) {
            handle(e, "Json not valid against schema.");
        } catch (final EnvelopeValidationException e) {
            envelopeValidationExceptionHandler.handle(e);
        }

    }

    private Metadata metadataOf(final JsonEnvelope envelope) {
        final Metadata metadata = envelope.metadata();
        if (metadata == null) {
            throw new EnvelopeValidationException("Metadata not set in the envelope.");
        }
        return metadata;
    }

    private void handle(final Exception e, final String message) {
        envelopeValidationExceptionHandler.handle(new EnvelopeValidationException(message, e));
    }
}
