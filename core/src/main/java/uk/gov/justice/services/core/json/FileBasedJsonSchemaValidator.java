package uk.gov.justice.services.core.json;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;

/**
 * Service for validating JSON payloads against a schema.
 */
@ApplicationScoped
public class FileBasedJsonSchemaValidator {

    private final Map<String, Schema> schemas = new ConcurrentHashMap<>();

    @Inject
    private JsonSchemaLoader jsonSchemaLoader;

    @Inject
    private PayloadExtractor payloadExtractor;

    @Inject
    private SchemaValidationErrorMessageGenerator schemaValidationErrorMessageGenerator;

    /**
     * Validate a JSON payload against the correct schema for the given message type name. If the
     * JSON contains metadata, this is removed first. Schemas are cached for reuse.
     *
     * @param envelopeJson the payload to validate
     * @param actionName   the message type name
     */
    public void validateWithoutSchemaCatalog(final String envelopeJson, final String actionName) {
        final JSONObject payload = payloadExtractor.extractPayloadFrom(envelopeJson);

        try {
            schemaOf(actionName).validate(payload);
        } catch (final ValidationException e) {
            throw new JsonSchemaValidationException(
                    schemaValidationErrorMessageGenerator.generateErrorMessage(e),
                    e);
        }
    }

    private Schema schemaOf(final String actionName) {
        return schemas.computeIfAbsent(actionName, jsonSchemaLoader::loadSchema);
    }
}
