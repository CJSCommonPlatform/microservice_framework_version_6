package uk.gov.justice.services.generators.commons.validator;

import static java.util.Arrays.asList;
import static uk.gov.justice.services.generators.commons.helper.Actions.responseMimeTypesOf;

import java.util.Collection;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;

/**
 * Checks if response mediaType is valid and contains a valid query name.
 */
public class ResponseContentTypeRamlValidator extends AbstractContentTypeRamlValidator {

    private static final String CONTENT_TYPE_DESCRIPTION = "response type";

    public ResponseContentTypeRamlValidator(final ActionType... actionTypes) {
        super(asList(actionTypes), CONTENT_TYPE_DESCRIPTION);
    }

    @Override
    protected Collection<MimeType> mediaTypesToValidate(final Action action) {
        return responseMimeTypesOf(action);
    }
}
