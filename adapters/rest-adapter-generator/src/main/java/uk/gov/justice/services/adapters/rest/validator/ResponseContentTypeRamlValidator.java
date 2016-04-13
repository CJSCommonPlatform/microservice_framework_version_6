package uk.gov.justice.services.adapters.rest.validator;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import uk.gov.justice.raml.common.validator.AbstractContentTypeRamlValidator;

import java.util.Collection;

import static uk.gov.justice.services.adapters.rest.generator.Actions.responseMimeTypesOf;

/**
 * Checks if response mediaType is valid and contains a valid query name.
 */
public class ResponseContentTypeRamlValidator extends AbstractContentTypeRamlValidator {

    public ResponseContentTypeRamlValidator() {
        super(ActionType.GET, "response type", "query");
    }

    @Override
    protected Collection<MimeType> mediaTypesToValidate(final Action action) {
        return responseMimeTypesOf(action);
    }
}
