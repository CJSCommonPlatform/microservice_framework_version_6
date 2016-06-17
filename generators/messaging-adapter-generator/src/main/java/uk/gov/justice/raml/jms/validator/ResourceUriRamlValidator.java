package uk.gov.justice.raml.jms.validator;

import static java.lang.String.format;

import uk.gov.justice.services.generators.commons.helper.MessagingResourceUri;
import uk.gov.justice.services.generators.commons.validator.AbstractResourceRamlValidator;
import uk.gov.justice.services.generators.commons.validator.RamlValidationException;

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
        if (!MessagingResourceUri.valid(uri)) {
            throw new RamlValidationException(format(ERROR_MSG, uri));
        }
    }

}
