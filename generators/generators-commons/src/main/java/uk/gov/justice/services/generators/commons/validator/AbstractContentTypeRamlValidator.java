package uk.gov.justice.services.generators.commons.validator;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.capitalize;

import java.util.Collection;
import java.util.List;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Resource;

public abstract class AbstractContentTypeRamlValidator extends AbstractResourceRamlValidator {

    private final List<ActionType> actionTypes;
    private final String contentTypeDescription;

    public AbstractContentTypeRamlValidator(final List<ActionType> actionTypes,
                                            final String contentTypeDescription) {
        this.actionTypes = actionTypes;
        this.contentTypeDescription = contentTypeDescription;
    }

    @Override
    protected void validate(final Resource resource) {
        actionTypes.forEach(actionType -> {
            final Action action = resource.getActions().get(actionType);

            if (action != null) {
                final Collection<MimeType> mediaTypes = mediaTypesToValidate(action);
                checkNonEmpty(mediaTypes);
            }
        });
    }

    protected abstract Collection<MimeType> mediaTypesToValidate(final Action action);

    private void checkNonEmpty(final Collection<MimeType> mediaTypes) {
        if (mediaTypes.isEmpty()) {
            throw new RamlValidationException(format("%s not set", capitalize(contentTypeDescription)));
        }
    }
}
