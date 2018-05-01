package uk.gov.justice.services.core.mapping;

import static java.util.Optional.ofNullable;

import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Constructs a cache of action name to media type from the List of {@link ActionNameToMediaTypesMapper}
 * beans provided by the {@link ActionNameToMediaTypesMappingObserver}.
 */
@ApplicationScoped
public class DefaultMediaTypesMappingCache implements MediaTypesMappingCache {

    @Inject
    MappingCacheInitialiser mappingCacheInitialiser;

    private Map<String, MediaTypes> actionNameToMediaTypesCache;

    @Override
    public synchronized Optional<MediaTypes> mediaTypesFor(final String actionName) {

        if (actionNameToMediaTypesCache == null) {
            actionNameToMediaTypesCache = mappingCacheInitialiser.initialiseCache();
        }

        return ofNullable(actionNameToMediaTypesCache.get(actionName));
    }
}
