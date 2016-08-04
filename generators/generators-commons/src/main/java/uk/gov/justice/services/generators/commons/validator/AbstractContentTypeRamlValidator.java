package uk.gov.justice.services.generators.commons.validator;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.capitalize;

import java.util.Collection;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Resource;

public abstract class AbstractContentTypeRamlValidator extends AbstractResourceRamlValidator {

    private final ActionType actionType;
    private final String contentTypeDescription;

    public AbstractContentTypeRamlValidator(final ActionType actionType,
                                            final String contentTypeDescription) {
        this.actionType = actionType;
        this.contentTypeDescription = contentTypeDescription;
    }

    @Override
    protected void validate(final Resource resource) {
        final Action postAction = resource.getActions().get(actionType);
        if (postAction != null) {
            final Collection<MimeType> mediaTypes = mediaTypesToValidate(postAction);
            checkNonEmpty(mediaTypes);
        }
    }

    protected abstract Collection<MimeType> mediaTypesToValidate(final Action postAction);

    private void checkNonEmpty(final Collection<MimeType> mediaTypes) {
        if (mediaTypes.isEmpty()) {
            throw new RamlValidationException(format("%s not set", capitalize(contentTypeDescription)));
        }
    }
}
