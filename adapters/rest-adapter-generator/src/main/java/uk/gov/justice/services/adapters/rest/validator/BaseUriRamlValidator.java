package uk.gov.justice.services.adapters.rest.validator;

import org.raml.model.Raml;
import uk.gov.justice.raml.common.validator.RamlValidationException;
import uk.gov.justice.raml.common.validator.RamlValidator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class BaseUriRamlValidator implements RamlValidator {
    private static final String ERROR_MSG = "Pillar and tier of service should be specified in the base uri: %s";
    private static final Pattern URI_PATTERN = Pattern
            .compile("\\S+(command/api|command/controller|command/handler|query/api|query/controller|query/view)\\S+");

    @Override
    public void validate(final Raml raml) {
        final String uri = raml.getBaseUri();
        final Matcher matcher = URI_PATTERN.matcher(uri);
        if (!matcher.matches()) {
            throw new RamlValidationException(format(ERROR_MSG, uri));
        }
    }
}
