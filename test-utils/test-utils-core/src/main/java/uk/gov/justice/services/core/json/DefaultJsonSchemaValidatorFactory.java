package uk.gov.justice.services.core.json;

import org.slf4j.LoggerFactory;

public class DefaultJsonSchemaValidatorFactory {

    public JsonSchemaValidator getDefaultJsonSchemaValidator() {

        final JsonSchemaLoader loader = new JsonSchemaLoader();
        loader.fileSystemUrlResolverStrategy = new DefaultFileSystemUrlResolverStrategy();

        final DefaultJsonSchemaValidator defaultJsonSchemaValidator = new DefaultJsonSchemaValidator();
        defaultJsonSchemaValidator.loader = loader;
        defaultJsonSchemaValidator.logger = LoggerFactory.getLogger(getClass());

        return defaultJsonSchemaValidator;
    }
}
