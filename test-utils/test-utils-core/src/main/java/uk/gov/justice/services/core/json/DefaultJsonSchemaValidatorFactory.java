package uk.gov.justice.services.core.json;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.schema.catalog.Catalog;
import uk.gov.justice.schema.catalog.CatalogObjectFactory;
import uk.gov.justice.schema.service.SchemaCatalogService;
import uk.gov.justice.services.core.mapping.NameToMediaTypeConverter;

public class DefaultJsonSchemaValidatorFactory {

    public JsonSchemaValidator getDefaultJsonSchemaValidator() {

        final JsonSchemaLoader loader = new JsonSchemaLoader();
        loader.fileSystemUrlResolverStrategy = new DefaultFileSystemUrlResolverStrategy();

        final PayloadExtractor payloadExtractor = new PayloadExtractor();

        final FileBasedJsonSchemaValidator fileBasedJsonSchemaValidator = new FileBasedJsonSchemaValidator();
        fileBasedJsonSchemaValidator.loader = loader;
        fileBasedJsonSchemaValidator.logger = getLogger(FileBasedJsonSchemaValidator.class);
        fileBasedJsonSchemaValidator.payloadExtractor = payloadExtractor;
        fileBasedJsonSchemaValidator.nameToMediaTypeConverter = new NameToMediaTypeConverter();

        final SchemaCatalogAwareJsonSchemaValidator schemaCatalogAwareJsonSchemaValidator = new SchemaCatalogAwareJsonSchemaValidator();
        schemaCatalogAwareJsonSchemaValidator.logger = getLogger(SchemaCatalogAwareJsonSchemaValidator.class);
        schemaCatalogAwareJsonSchemaValidator.fileBasedJsonSchemaValidator = fileBasedJsonSchemaValidator;
        schemaCatalogAwareJsonSchemaValidator.payloadExtractor = payloadExtractor;
        schemaCatalogAwareJsonSchemaValidator.schemaCatalogService = aSchemaCatalogService();
        schemaCatalogAwareJsonSchemaValidator.schemaIdMappingCache = new SchemaIdMappingCacheMock().initialize();

        return schemaCatalogAwareJsonSchemaValidator;
    }

    private SchemaCatalogService aSchemaCatalogService() {

        final Catalog catalog = new CatalogObjectFactory().catalog();
        final SchemaCatalogService schemaCatalogService = new SchemaCatalogService();

        try {
            setField(schemaCatalogService, "catalog", catalog);
        } catch (final IllegalAccessException e) {
            throw new InstantiationFailedException("Failed to set catalog on " + SchemaCatalogService.class.getSimpleName(), e);
        }

        return schemaCatalogService;
    }
}
