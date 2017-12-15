package uk.gov.justice.services.core.json;

import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.NameToMediaTypeConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 * Service for validating JSON payloads against a schema.
 */
@ApplicationScoped
public class FileBasedJsonSchemaValidator {

    private final Map<String, Schema> schemas = new ConcurrentHashMap<>();

    @Inject
    Logger logger;

    @Inject
    JsonSchemaLoader loader;

    @Inject
    PayloadExtractor payloadExtractor;

    @Inject
    NameToMediaTypeConverter nameToMediaTypeConverter;

    /**
     * Validate a JSON payload against the correct schema for the given message type name. If the
     * JSON contains metadata, this is removed first. Schemas are cached for reuse.
     *
     * @param envelopeJson the payload to validate
     * @param mediaType    the message type name
     */
    public void validate(final String envelopeJson, final MediaType mediaType) {
        final String name = nameToMediaTypeConverter.convert(mediaType);
        logger.trace("Performing schema validation for: {}", name);
        final JSONObject payload = payloadExtractor.extractPayloadFrom(envelopeJson);
        schemaOf(name).validate(payload);
    }

    private Schema schemaOf(final String name) {
        return schemas.computeIfAbsent(name, loader::loadSchema);
    }
}
