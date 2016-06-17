package uk.gov.justice.raml.jms.validator;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import uk.gov.justice.raml.jms.uri.BaseUri;
import uk.gov.justice.services.generators.commons.validator.RamlValidationException;
import uk.gov.justice.services.generators.commons.validator.RamlValidator;

import org.raml.model.Raml;

/**
 * Checks if RAML contain base uris that correctly adheres to the framework's standard.
 */
public class BaseUriRamlValidator implements RamlValidator {
    private static final String ERROR_MSG = "Invalid base uri: %s";
    private static final String BASE_URI_NOT_SET_MSG = "Base uri not set";

    @Override
    public void validate(final Raml raml) {
        if (isEmpty(raml.getBaseUri())) {
            throw new RamlValidationException(BASE_URI_NOT_SET_MSG);
        }
        if (!BaseUri.valid(raml.getBaseUri())) {
            throw new RamlValidationException(format(ERROR_MSG, raml.getBaseUri()));
        }
    }

}
