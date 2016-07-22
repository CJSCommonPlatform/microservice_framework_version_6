package uk.gov.justice.services.adapters.rest.validator;

import static uk.gov.justice.services.adapters.rest.generator.Actions.responseMimeTypesOf;

import uk.gov.justice.services.generators.commons.validator.AbstractContentTypeRamlValidator;

import java.util.Collection;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;

/**
 * Checks if response mediaType is valid and contains a valid query name.
 */
public class ResponseContentTypeRamlValidator extends AbstractContentTypeRamlValidator {

    private static final String CONTENT_TYPE_DESCRIPTION = "response type";

    public ResponseContentTypeRamlValidator() {
        super(ActionType.GET, CONTENT_TYPE_DESCRIPTION, false);
    }

    @Override
    protected Collection<MimeType> mediaTypesToValidate(final Action action) {
        return responseMimeTypesOf(action);
    }
}
