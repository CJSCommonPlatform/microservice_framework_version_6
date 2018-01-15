package uk.gov.justice.services.core.json;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.core.mapping.ActionNameToMediaTypesMapper;
import uk.gov.justice.services.core.mapping.MediaTypes;
import uk.gov.justice.services.core.mapping.MediaTypesMappingCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.reflections.Reflections;

public class MediaTypesMappingCacheMock implements MediaTypesMappingCache {

    private static final String DEFAULT_MAPPER_PACKAGE = "uk.gov.justice.api.mapper";

    private final Map<String, MediaTypes> nameToMediaTypes = new HashMap<>();

    public MediaTypesMappingCacheMock initialize() {
        return initialize(DEFAULT_MAPPER_PACKAGE);
    }

    public MediaTypesMappingCacheMock initialize(final String mapperPackage) {

        final Reflections reflections = new Reflections(mapperPackage);
        final Set<Class<? extends ActionNameToMediaTypesMapper>> classes = reflections.getSubTypesOf(ActionNameToMediaTypesMapper.class);

        final List<ActionNameToMediaTypesMapper> actionNameToMediaTypesMappers = classes.stream()
                .map(this::instantiate)
                .collect(toList());

        actionNameToMediaTypesMappers
                .forEach(mapper -> nameToMediaTypes.putAll(mapper.getActionNameToMediaTypesMap()));

        return this;
    }

    @Override
    public Optional<MediaTypes> mediaTypesFor(final String actionName) {
        return Optional.empty();
    }

    private ActionNameToMediaTypesMapper instantiate(final Class<? extends ActionNameToMediaTypesMapper> actionNameToMediaTypesMapperClass) {

        try {
            return actionNameToMediaTypesMapperClass.newInstance();
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new InstantiationFailedException(format("Failed to instantiate '%s' from its class", actionNameToMediaTypesMapperClass.getName()), e);
        }
    }
}
