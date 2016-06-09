package uk.gov.justice.services.clients.rest.generator.strategy;

import static uk.gov.justice.raml.common.mapper.ActionMapping.INVALID_ACTION_MAPPING_ERROR_MSG;

import uk.gov.justice.raml.common.mapper.ActionMapping;
import uk.gov.justice.raml.common.validator.RamlValidationException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.raml.model.ActionType;
import org.raml.model.MimeType;

/**
 * Action mapping generation strategy to support Action Mapping.
 */
public class ClientActionMappingGeneration implements ClientGenerationStrategy {

    /**
     * The list of {@link ActionMapping} parsed from the description of a RAML Action.
     *
     * @param description the description from a RAML Action
     * @return a list of {@link ActionMapping}
     */
    @Override
    public List<ActionMapping> listOfActionMappings(final String description) {
        return ActionMapping.listOf(description);
    }

    /**
     * Returns the {@link ActionMapping} from the list for the given MimeType and ActionType.
     *
     * @param actionMappings the list of {@link ActionMapping}
     * @param mimeType       the MimeType to use for the lookup
     * @param httpMethod     the ActionType to use for the lookup
     * @return the {@link ActionMapping}
     */
    @Override
    public Optional<ActionMapping> mappingOf(final Collection<ActionMapping> actionMappings, final MimeType mimeType, final ActionType httpMethod) {
        return actionMappings.stream().filter(m -> m.mimeTypeFor(httpMethod).equals(mimeType.getType())).findAny();
    }

    /**
     * Returns the handles annotation value taken from the {@link ActionMapping} name.
     *
     * @param mapping the {@link ActionMapping} to use.
     * @param header  the header is not used for this strategy.
     * @return the handles annotation value
     * @throws RamlValidationException if no mapping
     */
    @Override
    public String handlesValue(final Optional<ActionMapping> mapping, final String header) {
        return mapping.orElseThrow(() -> new RamlValidationException(INVALID_ACTION_MAPPING_ERROR_MSG)).getName();
    }
}
