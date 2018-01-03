package uk.gov.justice.services.generators.commons.mapping;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;

/**
 * Generate a Schema Id Mapping Class Name from the RAML base URI.
 */
public class SchemaMappingClassNameGenerator {

    /**
     * Generate Schema Id Mapping class name
     *
     * @param baseUri the base URI to process
     * @return the class name
     */
    public String createMappingClassNameFrom(final String baseUri, final Class<?> interfaceClass) {

        try {
            final String path = new URI(baseUri).getPath();
            final String contextName = path.substring(1, path.indexOf('/', 1));
            final String javaStyleContextName = of(contextName.split("-"))
                    .map(StringUtils::capitalize)
                    .collect(joining());

            return javaStyleContextName + interfaceClass.getSimpleName();
        } catch (final URISyntaxException e) {
            throw new RamlBaseUriSyntaxException(format("Failed to convert base uri from raml '%s' into a URI", baseUri), e);
        }
    }
}
