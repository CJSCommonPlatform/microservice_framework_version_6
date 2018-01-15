package uk.gov.justice.services.core.mapping;

import java.util.Optional;

public interface SchemaIdMappingCache {

    /**
     * Get the schema id for a given media type from the cache of {@link MediaType} to schema id.
     *
     * @param mediaType the {@link MediaType} to look up
     * @return Optional of the schema id or Optional.empty() if not found
     */
    Optional<String> schemaIdFor(final MediaType mediaType);
}
