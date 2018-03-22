package uk.gov.justice.services.generators.commons.mapping;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.StringUtils.capitalize;

import org.apache.commons.lang3.StringUtils;

/**
 * Generate a Schema Id Mapping Class Name from the RAML base URI.
 */
public class SubscriptionSchemaMappingClassNameGenerator {

    /**
     * Generate Schema Id Mapping class name
     *
     * @param contextName name of the context
     * @param componentName the name of the component
     * @param  interfaceClass name of the class to base the name on
     * @return the class name
     */
    public String createMappingClassNameFrom(final String contextName, final String componentName, final Class<?> interfaceClass) {

        final String javaStyleContextName = of(contextName.split("-"))
                .map(StringUtils::capitalize)
                .collect(joining());

        final String javaStyleComponentName = of(componentName.split("_"))
                .map(token -> capitalize(token.toLowerCase()))
                .collect(joining());

        return javaStyleContextName + javaStyleComponentName + interfaceClass.getSimpleName();
    }
}
