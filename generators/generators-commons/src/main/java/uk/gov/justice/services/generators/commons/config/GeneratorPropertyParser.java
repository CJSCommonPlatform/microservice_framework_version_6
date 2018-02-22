package uk.gov.justice.services.generators.commons.config;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import uk.gov.justice.raml.core.GeneratorConfig;

import java.util.Map;

public class GeneratorPropertyParser {

    private static final String SERVICE_COMPONENT_PROPERTY = "serviceComponent";
    private static final String CUSTOM_MDB_POOL_CONFIG_PROPERTY = "customMDBPool";
    private static final String NOT_SET_MESSAGE = "%s generator property not set in the plugin config";

    private final GeneratorConfig generatorConfig;

    public GeneratorPropertyParser(final GeneratorConfig generatorConfig) {
        this.generatorConfig = generatorConfig;
    }

    public String serviceComponent() {
        final Map<String, String> generatorProperties = generatorConfig.getGeneratorProperties();
        if (generatorProperties == null) {
            throw new IllegalArgumentException(format(NOT_SET_MESSAGE, SERVICE_COMPONENT_PROPERTY));
        }

        final String serviceComponentProperty = generatorProperties.get(SERVICE_COMPONENT_PROPERTY);
        if (isEmpty(serviceComponentProperty)) {
            throw new IllegalArgumentException(format(NOT_SET_MESSAGE, SERVICE_COMPONENT_PROPERTY));
        }

        return serviceComponentProperty;
    }

    public boolean shouldAddCustomPoolConfiguration() {
        final Map<String, String> generatorProperties = generatorConfig.getGeneratorProperties();
        if (generatorProperties == null) {
            throw new IllegalArgumentException(format(NOT_SET_MESSAGE, CUSTOM_MDB_POOL_CONFIG_PROPERTY));
        }

        return Boolean.valueOf(generatorProperties.get(CUSTOM_MDB_POOL_CONFIG_PROPERTY));
    }
}
