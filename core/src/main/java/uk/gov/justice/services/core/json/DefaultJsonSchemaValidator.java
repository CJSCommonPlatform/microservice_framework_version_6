package uk.gov.justice.services.core.json;

import static uk.gov.justice.services.messaging.JsonEnvelope.METADATA;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for validating JSON payloads against a schema.
 */
@ApplicationScoped
public class DefaultJsonSchemaValidator implements JsonSchemaValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJsonSchemaValidator.class);
    private final Map<String, Schema> schemas = new ConcurrentHashMap<>();

    private JsonSchemaLoader loader = new JsonSchemaLoader();

    /**
     * Validate a JSON payload against the correct schema for the given message type name. If the
     * JSON contains metadata, this is removed first. Schemas are cached for reuse.
     *
     * @param payload the payload to validate
     * @param name    the message type name
     */
    @Override
    public void validate(final String payload, final String name) {
        LOGGER.trace("Performing schema validation for: {}", name);
        final JSONObject jsonObject = new JSONObject(payload);
        jsonObject.remove(METADATA);
        schemaOf(name).validate(jsonObject);
    }

    private Schema schemaOf(final String name) {
        return schemas.computeIfAbsent(name, loader::loadSchema);
    }
}
