package uk.gov.justice.services.core.json;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypeToSchemaIdMapper;
import uk.gov.justice.services.core.mapping.SchemaIdMappingCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.reflections.Reflections;

public class SchemaIdMappingCacheMock implements SchemaIdMappingCache {

    private static final String DEFAULT_MAPPER_PACKAGE = "uk.gov.justice.api.mapper";

    private final Map<MediaType, String> mediaTypeToSchemaId = new HashMap<>();

    public SchemaIdMappingCacheMock initialize() {
        return initialize(DEFAULT_MAPPER_PACKAGE);
    }

    public SchemaIdMappingCacheMock initialize(final String mapperPackage) {

        final Reflections reflections = new Reflections(mapperPackage);
        final Set<Class<? extends MediaTypeToSchemaIdMapper>> classes = reflections.getSubTypesOf(MediaTypeToSchemaIdMapper.class);

        final List<MediaTypeToSchemaIdMapper> mediaTypeToSchemaIdMappers = classes.stream()
                .map(this::instantiate)
                .collect(toList());

        mediaTypeToSchemaIdMappers
                .forEach(mapper -> mediaTypeToSchemaId.putAll(mapper.getMediaTypeToSchemaIdMap()));

        return this;
    }
    
    @Override
    public Optional<String> schemaIdFor(final MediaType mediaType) {
        return ofNullable(mediaTypeToSchemaId.get(mediaType));
    }

    private MediaTypeToSchemaIdMapper instantiate(final Class<? extends MediaTypeToSchemaIdMapper> mediaTypeToSchemaIdMapperClass) {

        try {
            return mediaTypeToSchemaIdMapperClass.newInstance();
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new InstantiationFailedException(format("Failed to instantiate '%s' from its class", mediaTypeToSchemaIdMapperClass.getName()), e);
        }
    }
}
