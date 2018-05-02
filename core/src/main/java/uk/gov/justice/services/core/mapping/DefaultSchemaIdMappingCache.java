package uk.gov.justice.services.core.mapping;

import static java.util.Optional.ofNullable;

import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Constructs a cache of media type to schema id from the List of {@link MediaTypeToSchemaIdMapper}
 * beans provided by the {@link SchemaIdMappingObserver}.
 */
@ApplicationScoped
public class DefaultSchemaIdMappingCache implements SchemaIdMappingCache {

    @Inject
    SchemaIdMappingCacheInitialiser schemaIdMappingCacheInitialiser;

    private Map<MediaType, String> mediaTypeToSchemaIdCache;

    @Override
    public synchronized Optional<String> schemaIdFor(final MediaType mediaType) {

        if(mediaTypeToSchemaIdCache == null) {
          mediaTypeToSchemaIdCache = schemaIdMappingCacheInitialiser.initialiseCache();
        }

        return ofNullable(mediaTypeToSchemaIdCache.get(mediaType));
    }
}
