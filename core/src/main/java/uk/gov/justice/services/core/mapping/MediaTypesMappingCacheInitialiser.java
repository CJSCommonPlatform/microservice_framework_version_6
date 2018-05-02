package uk.gov.justice.services.core.mapping;

import static java.util.stream.Collectors.toMap;

import uk.gov.justice.services.core.extension.BeanInstantiater;

import java.util.Map;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;

@ApplicationScoped
public class MediaTypesMappingCacheInitialiser {

    @Inject
    ActionNameToMediaTypesMappingObserver actionNameToMediaTypesMappingObserver;

    @Inject
    BeanInstantiater beanInstantiater;

    public Map<String, MediaTypes> initialiseCache() {
        return actionNameToMediaTypesMappingObserver.getNameMediaTypesMappers().stream()
                .flatMap(this::mapEntriesFrom)
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (schemaId_1, schemaId_2) -> schemaId_1
                ));
    }

    private Stream<Map.Entry<String, MediaTypes>> mapEntriesFrom(final Bean<?> bean) {
        return ((ActionNameToMediaTypesMapper) beanInstantiater.instantiate(bean)).getActionNameToMediaTypesMap().entrySet().stream();
    }
}
