package uk.gov.justice.services.generators.commons.validator;

import java.util.Collection;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;

/**
 * Checks if httpAction's mediaType is valid and contains a valid command or event.
 */
public class RequestContentTypeRamlValidator extends AbstractContentTypeRamlValidator {

    private static final String CONTENT_TYPE_DESCRIPTION = "request type";

    public RequestContentTypeRamlValidator(final boolean generalJsonTypeAllowed) {
        super(ActionType.POST, CONTENT_TYPE_DESCRIPTION, generalJsonTypeAllowed);
    }

    @Override
    protected Collection<MimeType> mediaTypesToValidate(final Action postAction) {
        return postAction.getBody().values();
    }

}
