package uk.gov.justice.services.generators.commons.mapping;

import static com.google.common.base.CharMatcher.WHITESPACE;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trim;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.INVALID_ACTION_MAPPING_ERROR_MSG;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.MAPPING_BOUNDARY;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.NAME_KEY;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.REQUEST_TYPE_KEY;
import static uk.gov.justice.services.generators.commons.mapping.ActionMapping.RESPONSE_TYPE_KEY;

import uk.gov.justice.services.generators.commons.validator.RamlValidationException;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.raml.model.Action;
import org.raml.model.MimeType;

public class ActionMappingParser {

    private static final String MAPPING_SEPARATOR_PATTERN = "\\(mapping\\):";
    private static final String ACTION_NAME_NOT_SET_ERROR_MSG = "Invalid RAML file. Action name not defined in mapping";
    private static final String MEDIA_TYPE_NAME_NOT_SET_ERROR_MSG = "Invalid RAML file. Media type not defined in mapping";

    /**
     * Parses mappings string
     *
     * @param mappingsString - mapping string from raml file:
     *
     *                       <pre>
     *                            {@code
     *                                ...
     *                                   (mapping):
     *                                        requestType: application/vnd.people.command.create-user+json
     *                                        name: people.create-user
     *                                   (mapping):
     *                                        requestType: application/vnd.people.command.update-user+json
     *                                        name: people.update-user
     *                                ...
     *                             }
     *
     *                       </pre>
     * @return - collection of {@link ActionMapping} objects
     */
    public List<ActionMapping> listOf(final String mappingsString) {
        final List<ActionMapping> actionMappings = actionMappingsOf(trim(mappingsString));
        validate(actionMappings);
        return actionMappings;
    }

    public ActionMapping valueOf(final Action ramlAction, final MimeType mimeType) {
        final List<ActionMapping> actionMappings = listOf(ramlAction.getDescription());
        return actionMappings.stream()
                .filter(m -> m.mimeTypeFor(ramlAction.getType()).equals(mimeType.getType()))
                .findAny()
                .orElseThrow(() -> new RamlValidationException(INVALID_ACTION_MAPPING_ERROR_MSG));
    }


    private List<ActionMapping> actionMappingsOf(final String mappingString) {
        if (isNotEmpty(mappingString) && mappingString.contains(MAPPING_BOUNDARY)) {
            return stream(extractMappingFrom(mappingString)
                    .split(MAPPING_SEPARATOR_PATTERN))
                    .filter(StringUtils::isNotBlank)
                    .map(this::valueOf)
                    .collect(toList());

        }
        return emptyList();
    }

    private String extractMappingFrom(final String mappingString) {
        return trimPreMappingDescription(trimPostMappingDescription(mappingString));
    }

    private String trimPreMappingDescription(final String source) {
        if (isNotEmpty(source)) {
            return source.substring(source.indexOf(MAPPING_BOUNDARY) + MAPPING_BOUNDARY.length(), source.length());
        }
        return source;
    }

    private String trimPostMappingDescription(final String source) {
        return source.substring(0, source.lastIndexOf(MAPPING_BOUNDARY));
    }

    private ActionMapping valueOf(final String mappingString) {
        try {
            final Map<String, String> map = Splitter.on("\n")
                    .omitEmptyStrings()
                    .trimResults(WHITESPACE)
                    .withKeyValueSeparator(": ").split(mappingString);
            return new ActionMapping(map.get(NAME_KEY), map.get(REQUEST_TYPE_KEY), map.get(RESPONSE_TYPE_KEY));
        } catch (IllegalArgumentException ex) {
            throw new RamlValidationException(INVALID_ACTION_MAPPING_ERROR_MSG, ex);
        }
    }

    private void validate(final List<ActionMapping> actionMappings) {
        if (isEmpty(actionMappings)) {
            throw new RamlValidationException(INVALID_ACTION_MAPPING_ERROR_MSG);
        }
        validateNot(actionMappings, m -> isBlank(m.getName()), ACTION_NAME_NOT_SET_ERROR_MSG);
        validateNot(actionMappings, m -> isBlank(m.getRequestType()) && isBlank(m.getResponseType()),
                MEDIA_TYPE_NAME_NOT_SET_ERROR_MSG);
    }

    private void validateNot(final List<ActionMapping> actionMappings,
                             final Predicate<ActionMapping> actionMappingPredicate,
                             final String errorMessage) {
        if (actionMappings.stream().anyMatch(actionMappingPredicate)) {
            throw new RamlValidationException(errorMessage);
        }
    }
}
