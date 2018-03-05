package uk.gov.justice.raml.jms.core;

import uk.gov.justice.services.generators.commons.helper.MessagingAdapterBaseUri;
import uk.gov.justice.services.generators.commons.helper.MessagingResourceUri;

public class ClassNameFactory {

    private final MessagingAdapterBaseUri baseUri;
    private final MessagingResourceUri resourceUri;

    public ClassNameFactory(final MessagingAdapterBaseUri baseUri, final MessagingResourceUri resourceUri) {
        this.baseUri = baseUri;
        this.resourceUri = resourceUri;
    }

    /**
     * Convert given URI and component to a camel cased class name
     *
     * @param classNameSuffix class name suffix identifier
     * @return camel case class name
     */
    public String classNameWith(final String classNameSuffix) {
        return baseUri.toClassName() + resourceUri.toClassName() + classNameSuffix;
    }
}
