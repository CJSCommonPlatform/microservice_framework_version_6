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
public class MediaTypesMappingCacheInitialiserTest {

    @Mock
    private ActionNameToMediaTypesMappingObserver actionNameToMediaTypesMappingObserver;

    @Mock
    private BeanInstantiater beanInstantiater;

    @InjectMocks
    private MediaTypesMappingCacheInitialiser mediaTypesMappingCacheInitialiser;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldInitialiseTheMappingCache() throws Exception {
        final String actionName = "example.add-recipe";

        final MediaType requestMediaType = new MediaType("application/vnd.example.add-recipe+json");
        final MediaType responseMediaType = new MediaType("application/vnd.example.recipe-added+json");

        final Map<String, MediaTypes> mediaTypeToSchemaIdMap = of(actionName, new MediaTypes(requestMediaType, responseMediaType));

        final Bean<ActionNameToMediaTypesMapper> bean_1 = mock(Bean.class);
        final ActionNameToMediaTypesMapper actionNameToMediaTypesMapper = mock(ActionNameToMediaTypesMapper.class);

        when(actionNameToMediaTypesMappingObserver.getNameMediaTypesMappers()).thenReturn(singletonList(bean_1));
        when(beanInstantiater.instantiate(bean_1)).thenReturn(actionNameToMediaTypesMapper);
        when(actionNameToMediaTypesMapper.getActionNameToMediaTypesMap()).thenReturn(mediaTypeToSchemaIdMap);

        final Map<String, MediaTypes> mappingCache = mediaTypesMappingCacheInitialiser.initialiseCache();

        final MediaTypes mediaTypes = mappingCache.get(actionName);

        assertThat(mediaTypes.getRequestMediaType().get(), is(requestMediaType));
        assertThat(mediaTypes.getResponseMediaType().get(), is(responseMediaType));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldInitialiseTheMappingCacheWhenThereAreMoreThanOneMappingBeans() throws Exception {
        final String actionName_1 = "example.add-recipe";
        final MediaType requestMediaType_1 = new MediaType("application/vnd.example.add-recipe+json");
        final MediaType responseMediaType_1 = new MediaType("application/vnd.example.recipe-added+json");

        final String actionName_2 = "example.remove-recipe";
        final MediaType requestMediaType_2 = new MediaType("application/vnd.example.remove-recipe+json");
        final MediaType responseMediaType_2 = new MediaType("application/vnd.example.recipe-removed+json");

        final String actionName_3 = "example.update-recipe";
        final MediaType requestMediaType_3 = new MediaType("application/vnd.example.update-recipe+json");
        final MediaType responseMediaType_3 = new MediaType("application/vnd.example.recipe-updated+json");

        final Map<String, MediaTypes> mediaTypeToSchemaIdMap_1 = of(actionName_1, new MediaTypes(requestMediaType_1, responseMediaType_1), actionName_2, new MediaTypes(requestMediaType_2, responseMediaType_2));
        final Map<String, MediaTypes> mediaTypeToSchemaIdMap_2 = of(actionName_2, new MediaTypes(requestMediaType_2, responseMediaType_2), actionName_3, new MediaTypes(requestMediaType_3, responseMediaType_3));

        final Bean<ActionNameToMediaTypesMapper> bean_1 = mock(Bean.class);
        final Bean<ActionNameToMediaTypesMapper> bean_2 = mock(Bean.class);
        final ActionNameToMediaTypesMapper actionNameToMediaTypesMapper_1 = mock(ActionNameToMediaTypesMapper.class);
        final ActionNameToMediaTypesMapper actionNameToMediaTypesMapper_2 = mock(ActionNameToMediaTypesMapper.class);

        when(actionNameToMediaTypesMappingObserver.getNameMediaTypesMappers()).thenReturn(asList(bean_1, bean_2));
        when(beanInstantiater.instantiate(bean_1)).thenReturn(actionNameToMediaTypesMapper_1);
        when(beanInstantiater.instantiate(bean_2)).thenReturn(actionNameToMediaTypesMapper_2);
        when(actionNameToMediaTypesMapper_1.getActionNameToMediaTypesMap()).thenReturn(mediaTypeToSchemaIdMap_1);
        when(actionNameToMediaTypesMapper_2.getActionNameToMediaTypesMap()).thenReturn(mediaTypeToSchemaIdMap_2);


        final Map<String, MediaTypes> mappingCache = mediaTypesMappingCacheInitialiser.initialiseCache();

        final MediaTypes mediaTypes_1 = mappingCache.get(actionName_1);
        final MediaTypes mediaTypes_2 = mappingCache.get(actionName_2);
        final MediaTypes mediaTypes_3 = mappingCache.get(actionName_3);

        assertThat(mediaTypes_1.getRequestMediaType().get(), is(requestMediaType_1));
        assertThat(mediaTypes_1.getResponseMediaType().get(), is(responseMediaType_1));

        assertThat(mediaTypes_2.getRequestMediaType().get(), is(requestMediaType_2));
        assertThat(mediaTypes_2.getResponseMediaType().get(), is(responseMediaType_2));

        assertThat(mediaTypes_3.getRequestMediaType().get(), is(requestMediaType_3));
        assertThat(mediaTypes_3.getResponseMediaType().get(), is(responseMediaType_3));
    }
}
