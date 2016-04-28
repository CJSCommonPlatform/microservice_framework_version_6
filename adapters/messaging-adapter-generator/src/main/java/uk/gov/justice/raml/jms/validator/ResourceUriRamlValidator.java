package uk.gov.justice.raml.jms.validator;

import static java.lang.String.format;

import uk.gov.justice.raml.common.validator.AbstractResourceRamlValidator;
import uk.gov.justice.raml.common.validator.RamlValidationException;
import uk.gov.justice.raml.jms.uri.ResourceUri;

import org.raml.model.Resource;

/**
 * Checks if all resources in @Raml contain uris that correctly denote messaging component(s) of the
 * framework
 */
public class ResourceUriRamlValidator extends AbstractResourceRamlValidator {

    private static final String ERROR_MSG = "Invalid uri: %s";

    @Override
    protected void validate(final Resource resource) {
        final String uri = resource.getUri();
        if (!ResourceUri.valid(uri)) {
            throw new RamlValidationException(format(ERROR_MSG, uri));
        }
    }

}
