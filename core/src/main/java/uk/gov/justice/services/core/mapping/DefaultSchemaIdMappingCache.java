package uk.gov.justice.services.core.mapping;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import uk.gov.justice.services.core.extension.BeanInstantiater;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;

/**
 * Constructs a cache of media type to schema id from the List of {@link MediaTypeToSchemaIdMapper}
 * beans provided by the {@link SchemaIdMappingObserver}.
 */
@ApplicationScoped
public class DefaultSchemaIdMappingCache implements SchemaIdMappingCache {

    @Inject
    SchemaIdMappingObserver schemaIdMappingObserver;

    @Inject
    BeanInstantiater beanInstantiater;

    private Map<MediaType, String> mediaTypeToSchemaIdCache;

    /**
     * Gets the List of {@link MediaTypeToSchemaIdMapper} beans, instantiates them and collects all
     * entries into a single Map of media type to schema id.
     */
    @PostConstruct
    public void initialise() {
        mediaTypeToSchemaIdCache = schemaIdMappingObserver.getMediaTypeToSchemaIdMappers().stream()
                .flatMap(this::mapEntriesFrom)
                .collect(toMap(
                        Entry::getKey,
                        Entry::getValue,
                        (schemaId_1, schemaId_2) -> schemaId_1
                ));
    }

    @Override
    public Optional<String> schemaIdFor(final MediaType mediaType) {
        return ofNullable(mediaTypeToSchemaIdCache.get(mediaType));
    }

    private Stream<Entry<MediaType, String>> mapEntriesFrom(final Bean<?> bean) {
        return ((MediaTypeToSchemaIdMapper) beanInstantiater.instantiate(bean)).getMediaTypeToSchemaIdMap().entrySet().stream();
    }
}
