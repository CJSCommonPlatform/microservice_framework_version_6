package uk.gov.justice.services.core.json;

import static java.lang.String.format;

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
public class SchemaCatalogAwareJsonSchemaValidator {

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

    public void validate(final String envelopeJson, final String actionName, final Optional<MediaType> mediaTypeOptional) {

        mediaTypeOptional.ifPresent(mediaType -> doValidate(envelopeJson, actionName, mediaType));
    }

    private void doValidate(final String envelopeJson, final String actionName, final MediaType mediaType) {
        final Optional<Schema> schema = schemaIdMappingCache.schemaIdFor(mediaType).flatMap(schemaCatalogService::findSchema);

        if (schema.isPresent()) {
            logger.info(format("Performing schema validation with catalog schema for action '%s' and mediaType '%s", actionName, mediaType));
            final JSONObject payload = payloadExtractor.extractPayloadFrom(envelopeJson);
            schema.get().validate(payload);
        } else {
            fileBasedJsonSchemaValidator.validateWithoutSchemaCatalog(envelopeJson, actionName);
        }
    }
}
