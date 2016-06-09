package uk.gov.justice.services.clients.rest.generator.strategy;

import uk.gov.justice.raml.common.mapper.ActionMapping;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.raml.model.ActionType;
import org.raml.model.MimeType;

public interface ClientGenerationStrategy {

    /**
     * Returns the list of {@link ActionMapping} for this generation strategy.
     *
     * @param description the description from a RAML Action
     * @return a list of {@link ActionMapping}
     */
    List<ActionMapping> listOfActionMappings(final String description);

    /**
     * Returns the {@link ActionMapping} for this generation strategy.
     *
     * @param actionMappings the list of {@link ActionMapping}
     * @param mimeType       the MimeType to use for the lookup
     * @param httpMethod     the ActionType to use for the lookup
     * @return the {@link ActionMapping}
     */
    Optional<ActionMapping> mappingOf(final Collection<ActionMapping> actionMappings, final MimeType mimeType, final ActionType httpMethod);

    /**
     * Returns the handles annotation value for this generation strategy.
     *
     * @param mapping the optional {@link ActionMapping} to use.
     * @param header  the header to use
     * @return the handles annotation value
     */
    String handlesValue(Optional<ActionMapping> mapping, String header);
}
