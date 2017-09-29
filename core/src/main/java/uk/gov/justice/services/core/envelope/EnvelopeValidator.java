package uk.gov.justice.services.core.envelope;


import static java.lang.String.format;
import static javax.json.JsonValue.NULL;

import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.core.json.SchemaLoadingException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import javax.json.JsonValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.ValidationException;

public class EnvelopeValidator {

    private final ObjectMapper objectMapper;
    private final JsonSchemaValidator jsonSchemaValidator;
    private final EnvelopeValidationExceptionHandler envelopeValidationExceptionHandler;
    private final String component;

    public EnvelopeValidator(final JsonSchemaValidator jsonSchemaValidator,
                             final EnvelopeValidationExceptionHandler jsonValidationExceptionHandler,
                             final ObjectMapper objectMapper,
                             final String component) {
        this.jsonSchemaValidator = jsonSchemaValidator;
        this.envelopeValidationExceptionHandler = jsonValidationExceptionHandler;
        this.objectMapper = objectMapper;
        this.component = component;
    }

    public void validate(final JsonEnvelope envelope) {
        try {
            final JsonValue payload = envelope.payload();
            if (!NULL.equals(payload)) {
                jsonSchemaValidator.validate(
                        objectMapper.writeValueAsString(payload), component + "/" + metadataOf(envelope).name());
            }
        } catch (final JsonProcessingException e) {
            handle(e, "Error serialising json.");
        } catch (SchemaLoadingException e) {
            handle(e, format("Could not load json schema that matches message type %s.", envelope.metadata().name()));
        } catch (final ValidationException e) {
            handle(e, format("Message not valid against schema: \n%s", envelope.toObfuscatedDebugString()));
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
