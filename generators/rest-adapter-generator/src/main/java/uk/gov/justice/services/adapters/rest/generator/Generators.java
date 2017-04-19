package uk.gov.justice.services.adapters.rest.generator;


import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.isBlank;
import static uk.gov.justice.services.generators.commons.helper.Names.buildJavaFriendlyName;

import uk.gov.justice.services.generators.commons.helper.RestResourceBaseUri;

import java.util.Comparator;

import org.raml.model.MimeType;
import org.raml.model.Resource;

final class Generators {
    private static final String INTERFACE_NAME_SUFFIX = "Resource";


    private Generators() {
    }


    /**
     * Comparator for ordering MimeType by the string representation returned by getType().
     *
     * @return the MimeType Comparator
     */
    static Comparator<MimeType> byMimeTypeOrder() {
        return comparing(MimeType::getType);
    }

    static String resourceInterfaceNameOf(final Resource resource, final RestResourceBaseUri baseUri) {
        final String resourceInterfaceName = buildJavaFriendlyName(format("%s%s", baseUri.classNamePrefix(), defaultIfBlank(resource.getDisplayName(),
                resource.getRelativeUri())));

        return isBlank(resourceInterfaceName) ? "Root" : resourceInterfaceName.concat(INTERFACE_NAME_SUFFIX);
    }

    static String resourceImplementationNameOf(final Resource resource, final RestResourceBaseUri baseUri) {
        return format("Default%s", resourceInterfaceNameOf(resource, baseUri));
    }

}
