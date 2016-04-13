package uk.gov.justice.raml.common.validator;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;

import java.util.Collection;

/**
 * Checks if action's mediaType is valid and contains a valid command or event.
 */
public class RequestContentTypeRamlValidator extends AbstractContentTypeRamlValidator {

    public RequestContentTypeRamlValidator() {
        super(ActionType.POST, "request type", "command", "event");
    }

    @Override
    protected Collection<MimeType> mediaTypesToValidate(final Action postAction) {
        return postAction.getBody().values();
    }

}
