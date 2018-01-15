package uk.gov.justice.services.core.mapping;

import java.util.Optional;

public interface MediaTypesMappingCache {

    Optional<MediaTypes> mediaTypesFor(final String actionName);
}
