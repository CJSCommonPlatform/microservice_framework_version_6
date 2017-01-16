package uk.gov.justice.services.generators.commons.validator;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.listOf;

import uk.gov.justice.services.generators.commons.helper.Actions;

import java.util.Collection;
import java.util.Set;

import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Resource;

/**
 * Validates action mapping against resource
 */
public class ActionMappingRamlValidator extends AbstractResourceRamlValidator {

    @Override
    protected void validate(final Resource resource) {
        resource.getActions().values().forEach(ramlAction -> {
            final Set<String> mediaTypesInMapping =
                    listOf(ramlAction.getDescription())
                            .stream()
                            .map(am -> am.mimeTypeFor(ramlAction.getType()))
                            .collect(toSet());
            final Set<String> notMappedMediaTypes =
                    mimeTypesOf(ramlAction)
                            .stream()
                            .map(MimeType::getType)
                            .filter(ramlActionMediaType -> !mediaTypesInMapping.contains(ramlActionMediaType))
                            .collect(toSet());
            if (!notMappedMediaTypes.isEmpty()) {
                throw new RamlValidationException(format("Invalid RAML file. Media type(s) not mapped to an action: %s", notMappedMediaTypes));
            }

        });
    }

    private Collection<MimeType> mimeTypesOf(final Action ramlAction) {
        if (Actions.isSupportedActionTypeWithRequestType(ramlAction.getType())) {
            return ramlAction.getBody().values();
        } else {
            return ramlAction.getResponses().values().stream()
                    .flatMap(response -> response.getBody().values().stream())
                    .collect(toList());
        }
    }
}
