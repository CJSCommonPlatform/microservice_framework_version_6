package uk.gov.justice.raml.jms.validation;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Resource;

/**
 * Checks if action's mediaType is valid and contains a valid command or event.
 *
 */
public class MediaTypeRamlValidator extends AbstractResourceRamlValidator {

    private static final Pattern MEDIA_TYPE_PATTERN = Pattern
            .compile("application/vnd\\.\\S+\\.(commands|events)\\.\\S+\\+json");

    @Override
    protected void validate(final Resource resource) {
        Action postAction = resource.getActions().get(ActionType.POST);
        if (postAction != null) {
            Collection<MimeType> mediaTypes = postAction.getBody().values();
            checkNonEmpty(mediaTypes);
            checkValid(mediaTypes);
        }

    }

    private void checkValid(final Collection<MimeType> mediaTypes) {
        mediaTypes.forEach(mt -> {
            Matcher matcher = MEDIA_TYPE_PATTERN.matcher(mt.getType());
            if (!matcher.matches()) {
                throw new RamlValidationException(String.format("Invalid media type: %s", mt.getType()));
            }
        });
    }

    private void checkNonEmpty(final Collection<MimeType> mediaTypes) {
        if (mediaTypes.isEmpty()) {
            throw new RamlValidationException("No declared media types");
        }
    }

}
