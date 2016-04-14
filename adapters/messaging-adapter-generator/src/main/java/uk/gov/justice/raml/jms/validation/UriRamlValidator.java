package uk.gov.justice.raml.jms.validation;

import static java.lang.String.format;

import uk.gov.justice.raml.common.validator.AbstractResourceRamlValidator;
import uk.gov.justice.raml.common.validator.RamlValidationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.raml.model.Resource;

/**
 * Checks if all resources in @Raml contain uris that correctly denote messaging component(s) of the framework
 *
 */
public class UriRamlValidator extends AbstractResourceRamlValidator {

    private static final String ERROR_MSG = "Inavlid uri: %s";
    private static final Pattern URI_PATTERN = Pattern
            .compile("\\S+\\.(api.command|controller.command|handler.command|event)");

    @Override
    protected void validate(final Resource resource) {
        final String uri = resource.getUri();
        final Matcher matcher = URI_PATTERN.matcher(uri);
        if (!matcher.matches()) {
            throw new RamlValidationException(format(ERROR_MSG, uri));
        }
    }

}
