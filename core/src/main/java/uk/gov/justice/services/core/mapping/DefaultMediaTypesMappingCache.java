package uk.gov.justice.services.core.mapping;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import uk.gov.justice.services.core.extension.BeanInstantiater;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;

/**
 * Constructs a cache of action name to media type from the List of {@link ActionNameToMediaTypesMapper}
 * beans provided by the {@link ActionNameToMediaTypesMappingObserver}.
 */
public class DefaultMediaTypesMappingCache implements MediaTypesMappingCache {

    @Inject
    ActionNameToMediaTypesMappingObserver actionNameToMediaTypesMappingObserver;

    @Inject
    BeanInstantiater beanInstantiater;

    private Map<String, MediaTypes> actionNameToMediaTypesCache;

    /**
     * Gets the List of {@link ActionNameToMediaTypesMapper} beans, instantiates them and collects all
     * entries into a single Map of action name to media type.
     */
    @PostConstruct
    public void initialise() {
        actionNameToMediaTypesCache = actionNameToMediaTypesMappingObserver.getNameMediaTypesMappers().stream()
                .flatMap(this::mapEntriesFrom)
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (schemaId_1, schemaId_2) -> schemaId_1
                ));
    }

    @Override
    public Optional<MediaTypes> mediaTypesFor(final String actionName) {
        return ofNullable(actionNameToMediaTypesCache.get(actionName));
    }

    private Stream<Map.Entry<String, MediaTypes>> mapEntriesFrom(final Bean<?> bean) {
        return ((ActionNameToMediaTypesMapper) beanInstantiater.instantiate(bean)).getActionNameToMediaTypesMap().entrySet().stream();
    }
}
