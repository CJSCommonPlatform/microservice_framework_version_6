package uk.gov.justice.services.core.json;

import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.schema.catalog.Catalog;
import uk.gov.justice.schema.catalog.CatalogObjectFactory;
import uk.gov.justice.schema.service.SchemaCatalogResolverProducer;
import uk.gov.justice.schema.service.SchemaCatalogService;

public class DefaultJsonSchemaValidatorFactory {

    public JsonSchemaValidator getDefaultJsonSchemaValidator() {

        final JsonSchemaLoader loader = new JsonSchemaLoader();
        final SchemaCatalogResolverProducer schemaCatalogResolverProducer = new SchemaCatalogResolverProducer();
        loader.schemaCatalogResolver = schemaCatalogResolverProducer.schemaCatalogResolver();

        final PayloadExtractor payloadExtractor = new PayloadExtractor();

        final FileBasedJsonSchemaValidator fileBasedJsonSchemaValidator = new FileBasedJsonSchemaValidator();

        setField(fileBasedJsonSchemaValidator, "jsonSchemaLoader", loader);
        setField(fileBasedJsonSchemaValidator, "payloadExtractor", payloadExtractor);
        setField(fileBasedJsonSchemaValidator, "schemaValidationErrorMessageGenerator", new SchemaValidationErrorMessageGenerator());

        final SchemaCatalogAwareJsonSchemaValidator schemaCatalogAwareJsonSchemaValidator = new SchemaCatalogAwareJsonSchemaValidator();
        schemaCatalogAwareJsonSchemaValidator.fileBasedJsonSchemaValidator = fileBasedJsonSchemaValidator;
        schemaCatalogAwareJsonSchemaValidator.payloadExtractor = payloadExtractor;
        schemaCatalogAwareJsonSchemaValidator.schemaCatalogService = aSchemaCatalogService();
        schemaCatalogAwareJsonSchemaValidator.schemaIdMappingCache = new SchemaIdMappingCacheMock().initialize();

        final BackwardsCompatibleJsonSchemaValidator backwardsCompatibleJsonSchemaValidator = new BackwardsCompatibleJsonSchemaValidator();

        backwardsCompatibleJsonSchemaValidator.schemaCatalogAwareJsonSchemaValidator = schemaCatalogAwareJsonSchemaValidator;
        backwardsCompatibleJsonSchemaValidator.fileBasedJsonSchemaValidator = fileBasedJsonSchemaValidator;

        return backwardsCompatibleJsonSchemaValidator;
    }

    private SchemaCatalogService aSchemaCatalogService() {

        final Catalog catalog = new CatalogObjectFactory().catalog();
        final SchemaCatalogService schemaCatalogService = new SchemaCatalogService();

        setField(schemaCatalogService, "catalog", catalog);

        return schemaCatalogService;
    }
}
