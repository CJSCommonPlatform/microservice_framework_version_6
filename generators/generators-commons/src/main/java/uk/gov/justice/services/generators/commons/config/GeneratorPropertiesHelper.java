package uk.gov.justice.services.generators.commons.config;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorConfig;

public final class GeneratorPropertiesHelper {

    private static final String SERVICE_COMPONENT_PROPERTY = "serviceComponent";
    private static final String NOT_SET_MESSAGE = "%s generator property not set in the plugin config";

    private GeneratorPropertiesHelper() {
    }

    public static String serviceComponentOf(final GeneratorConfig generatorConfig) {
        final CommonGeneratorProperties
                generatorProperties = (CommonGeneratorProperties) generatorConfig.getGeneratorProperties();

        if (generatorProperties == null) {
            throw new IllegalArgumentException(format(NOT_SET_MESSAGE, SERVICE_COMPONENT_PROPERTY));
        }

        final String serviceComponentProperty = generatorProperties.getServiceComponent();
        if (isEmpty(serviceComponentProperty)) {
            throw new IllegalArgumentException(format(NOT_SET_MESSAGE, SERVICE_COMPONENT_PROPERTY));
        }

        return serviceComponentProperty;
    }

}
