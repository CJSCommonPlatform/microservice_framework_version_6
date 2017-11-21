package uk.gov.justice.services.core.json;

import uk.gov.justice.schema.service.SchemaCatalogService;
import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.SchemaIdMappingCache;

import java.util.Optional;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 * Service for validating JSON payloads against a schema contained in a catalog.
 */
@ApplicationScoped
public class SchemaCatalogAwareJsonSchemaValidator implements JsonSchemaValidator {

    @Inject
    Logger logger;

    @Inject
    FileBasedJsonSchemaValidator fileBasedJsonSchemaValidator;

    @Inject
    SchemaIdMappingCache schemaIdMappingCache;

    @Inject
    SchemaCatalogService schemaCatalogService;

    @Inject
    PayloadExtractor payloadExtractor;

    /**
     * Validate a JSON payload against a schema contained in the schema catalog for the given message
     * type name. If the JSON contains metadata, this is removed first.  If no schema for the media type
     * can be found then it falls back to checking for schemas on the class path.
     *
     * @param envelopeJson the payload to validate
     * @param mediaType    the message type
     */
    @Override
    public void validate(final String envelopeJson, final MediaType mediaType) {

        final Optional<Schema> schema = getSchema(mediaType);
        if (schema.isPresent()) {
            logger.info("Performing schema validation with catalog schema for: {}", mediaType);
            final JSONObject payload = payloadExtractor.extractPayloadFrom(envelopeJson);
            schema.get().validate(payload);
        } else {
            logger.info("Falling back to file base schema lookup, no catalog schema found for: {}", mediaType);
            doValidationWithoutMappedSchema(envelopeJson, mediaType);
        }
    }

    private Optional<Schema> getSchema(final MediaType mediaType) {
        return schemaIdMappingCache.schemaIdFor(mediaType)
                .flatMap(schemaCatalogService::findSchema);
    }

    private void doValidationWithoutMappedSchema(final String envelopeJson, final MediaType mediaType) {
        fileBasedJsonSchemaValidator.validate(envelopeJson, mediaType);
    }
}
