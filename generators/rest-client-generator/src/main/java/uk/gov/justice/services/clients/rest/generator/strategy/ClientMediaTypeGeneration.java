package uk.gov.justice.services.clients.rest.generator.strategy;

import static java.util.Collections.emptyList;

import uk.gov.justice.services.generators.commons.mapping.ActionMapping;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.raml.model.ActionType;
import org.raml.model.MimeType;

/**
 * Media type generation strategy to support backwards compatibility.
 */
public class ClientMediaTypeGeneration implements ClientGenerationStrategy {

    /**
     * Returns an empty list as Action Mapping is not used.
     *
     * @param description the description from a RAML Action
     * @return an empty list
     */
    @Override
    public List<ActionMapping> listOfActionMappings(final String description) {
        return emptyList();
    }

    /**
     * Returns Optional.empty as this is not needed for backwards compatibility.
     *
     * @param actionMappings the list of {@link ActionMapping} is not used
     * @param mimeType       the MimeType is not used
     * @param httpMethod     the ActionType is not used
     * @return Optional.empty
     */
    @Override
    public Optional<ActionMapping> mappingOf(final Collection<ActionMapping> actionMappings, final MimeType mimeType, final ActionType httpMethod) {
        return Optional.empty();
    }

    /**
     * Returns the header as tha handles annotation value.
     *
     * @param mapping the optional {@link ActionMapping} is not used.
     * @param header  the header.
     * @return the handles annotation value
     */
    @Override
    public String handlesValue(final Optional<ActionMapping> mapping, final String header) {
        return header;
    }

}
