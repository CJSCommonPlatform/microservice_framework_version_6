package uk.gov.justice.services.generators.commons.validator;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.capitalize;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Resource;

public abstract class AbstractContentTypeRamlValidator extends AbstractResourceRamlValidator {
    private final Pattern mediaTypePattern;
    private final ActionType actionType;
    private final String contentTypeDescription;

    public AbstractContentTypeRamlValidator(final ActionType actionType,
                                            final String contentTypeDescription,
                                            final boolean generalJsonTypeAllowed) {
        this.actionType = actionType;
        this.contentTypeDescription = contentTypeDescription;
        mediaTypePattern = Pattern.compile(generalJsonTypeAllowed ? "application/(vnd\\.\\S+\\+)?json" : "application/vnd\\.\\S+\\+json");
    }

    @Override
    protected void validate(final Resource resource) {
        final Action postAction = resource.getActions().get(actionType);
        if (postAction != null) {
            final Collection<MimeType> mediaTypes = mediaTypesToValidate(postAction);
            checkNonEmpty(mediaTypes);
            checkValid(mediaTypes);
        }
    }

    protected abstract Collection<MimeType> mediaTypesToValidate(final Action postAction);

    private void checkValid(final Collection<MimeType> mediaTypes) {
        mediaTypes.forEach(mt -> {
            final Matcher matcher = mediaTypePattern.matcher(mt.getType());
            if (!matcher.matches()) {
                throw new RamlValidationException(format("Invalid %s: %s", contentTypeDescription, mt.getType()));
            }
        });
    }

    private void checkNonEmpty(final Collection<MimeType> mediaTypes) {
        if (mediaTypes.isEmpty()) {
            throw new RamlValidationException(format("%s not set", capitalize(contentTypeDescription)));
        }
    }
}
