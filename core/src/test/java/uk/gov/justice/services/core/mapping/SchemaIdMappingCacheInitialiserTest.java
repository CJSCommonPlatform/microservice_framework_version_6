package uk.gov.justice.services.core.mapping;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.extension.BeanInstantiater;

import java.util.Map;

import javax.enterprise.inject.spi.Bean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SchemaIdMappingCacheInitialiserTest {

    @Mock
    private SchemaIdMappingObserver schemaIdMappingObserver;

    @Mock
    private BeanInstantiater beanInstantiater;

    @InjectMocks
    private SchemaIdMappingCacheInitialiser schemaIdMappingCacheInitialiser;

    @Test
    @SuppressWarnings("unchecked")
    public void shouldInitialiseCacheWithSingleMappingBean() throws Exception {
        final MediaType mediaType = new MediaType("application/vnd.example.add-recipe+json");
        final String schemaId = "http://justice.gov.uk/example/command/api/example.add-recipe.json";

        final Map<MediaType, String> mediaTypeToSchemaIdMap = of(mediaType, schemaId);

        final Bean<MediaTypeToSchemaIdMapper> bean_1 = mock(Bean.class);
        final MediaTypeToSchemaIdMapper mediaTypeToSchemaIdMapper = mock(MediaTypeToSchemaIdMapper.class);

        when(schemaIdMappingObserver.getMediaTypeToSchemaIdMappers()).thenReturn(singletonList(bean_1));
        when(beanInstantiater.instantiate(bean_1)).thenReturn(mediaTypeToSchemaIdMapper);
        when(mediaTypeToSchemaIdMapper.getMediaTypeToSchemaIdMap()).thenReturn(mediaTypeToSchemaIdMap);

        final Map<MediaType, String> mappingCache = schemaIdMappingCacheInitialiser.initialiseCache();

        assertThat(mappingCache.size(), is(1));

        assertThat(mappingCache.get(mediaType), is(schemaId));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldInitialiseCacheWithMultipleMappingBeans() throws Exception {
        final MediaType mediaType_1 = new MediaType("application/vnd.example.add-recipe+json");
        final String schemaId_1 = "http://justice.gov.uk/example/command/api/example.add-recipe.json";

        final MediaType mediaType_2 = new MediaType("application/vnd.example.remove-recipe+json");
        final String schemaId_2 = "http://justice.gov.uk/example/command/api/example.remove-recipe.json";

        final MediaType mediaType_3 = new MediaType("application/vnd.example.update-recipe+json");
        final String schemaId_3 = "http://justice.gov.uk/example/command/api/example.update-recipe.json";

        final Map<MediaType, String> mediaTypeToSchemaIdMap_1 = of(mediaType_1, schemaId_1, mediaType_2, schemaId_2);
        final Map<MediaType, String> mediaTypeToSchemaIdMap_2 = of(mediaType_3, schemaId_3);

        final Bean<MediaTypeToSchemaIdMapper> bean_1 = mock(Bean.class);
        final Bean<MediaTypeToSchemaIdMapper> bean_2 = mock(Bean.class);
        final MediaTypeToSchemaIdMapper mediaTypeToSchemaIdMapper_1 = mock(MediaTypeToSchemaIdMapper.class);
        final MediaTypeToSchemaIdMapper mediaTypeToSchemaIdMapper_2 = mock(MediaTypeToSchemaIdMapper.class);

        when(schemaIdMappingObserver.getMediaTypeToSchemaIdMappers()).thenReturn(asList(bean_1, bean_2));
        when(beanInstantiater.instantiate(bean_1)).thenReturn(mediaTypeToSchemaIdMapper_1);
        when(beanInstantiater.instantiate(bean_2)).thenReturn(mediaTypeToSchemaIdMapper_2);
        when(mediaTypeToSchemaIdMapper_1.getMediaTypeToSchemaIdMap()).thenReturn(mediaTypeToSchemaIdMap_1);
        when(mediaTypeToSchemaIdMapper_2.getMediaTypeToSchemaIdMap()).thenReturn(mediaTypeToSchemaIdMap_2);

        final Map<MediaType, String> mappingCache = schemaIdMappingCacheInitialiser.initialiseCache();

        assertThat(mappingCache.size(), is(3));

        assertThat(mappingCache.get(mediaType_1),is(schemaId_1));
        assertThat(mappingCache.get(mediaType_2),is(schemaId_2));
        assertThat(mappingCache.get(mediaType_3),is(schemaId_3));
    }
}
