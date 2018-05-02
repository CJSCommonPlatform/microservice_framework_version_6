package uk.gov.justice.services.core.mapping;

import static java.util.stream.Collectors.toMap;

import uk.gov.justice.services.core.extension.BeanInstantiater;

import java.util.Map;
import java.util.stream.Stream;

import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;

public class SchemaIdMappingCacheInitialiser {

    @Inject
    SchemaIdMappingObserver schemaIdMappingObserver;

    @Inject
    BeanInstantiater beanInstantiater;

    public Map<MediaType, String> initialiseCache() {
        return schemaIdMappingObserver.getMediaTypeToSchemaIdMappers().stream()
                .flatMap(this::mapEntriesFrom)
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (schemaId_1, schemaId_2) -> schemaId_1
                ));
    }

    private Stream<Map.Entry<MediaType, String>> mapEntriesFrom(final Bean<?> bean) {
        return ((MediaTypeToSchemaIdMapper) beanInstantiater.instantiate(bean)).getMediaTypeToSchemaIdMap().entrySet().stream();
    }
}
