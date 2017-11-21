package uk.gov.justice.services.core.mapping;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.extension.BeanInstantiater;

import java.util.Map;
import java.util.Optional;

import javax.enterprise.inject.spi.Bean;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class DefaultMediaTypesMappingCacheTest {

    @Mock
    private ActionNameToMediaTypesMappingObserver schemaIdMappingObserver;

    @Mock
    private BeanInstantiater beanInstantiater;

    @InjectMocks
    private DefaultMediaTypesMappingCache defaultMediaTypesMappingCache;


    @Test
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public void shouldReturnMediaTypesForActionNameAfterInitialisedWithSingleMappingBean() throws Exception {

        final String actionName = "example.add-recipe";

        final MediaType requestMediaType = new MediaType("application/vnd.example.add-recipe+json");
        final MediaType responseMediaType = new MediaType("application/vnd.example.recipe-added+json");

        final MediaTypes mediaTypes = new MediaTypes(requestMediaType, responseMediaType);

        final Map<String, MediaTypes> mediaTypeToSchemaIdMap = of(actionName, mediaTypes);

        final Bean<ActionNameToMediaTypesMapper> bean_1 = mock(Bean.class);
        final ActionNameToMediaTypesMapper actionNameToMediaTypesMapper = mock(ActionNameToMediaTypesMapper.class);

        when(schemaIdMappingObserver.getNameMediaTypesMappers()).thenReturn(singletonList(bean_1));
        when(beanInstantiater.instantiate(bean_1)).thenReturn(actionNameToMediaTypesMapper);
        when(actionNameToMediaTypesMapper.getActionNameToMediaTypesMap()).thenReturn(mediaTypeToSchemaIdMap);

        defaultMediaTypesMappingCache.initialise();

        final Optional<MediaTypes> mediaTypesOptional = defaultMediaTypesMappingCache.mediaTypesFor(actionName);

        assertThat(mediaTypesOptional.get().getRequestMediaType(), is(of(requestMediaType)));
    }

    @Test
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public void shouldReturnMediaTypesForActionNameAfterInitialisedWithMultipleMappingBeans() throws Exception {

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

        when(schemaIdMappingObserver.getNameMediaTypesMappers()).thenReturn(asList(bean_1, bean_2));
        when(beanInstantiater.instantiate(bean_1)).thenReturn(actionNameToMediaTypesMapper_1);
        when(beanInstantiater.instantiate(bean_2)).thenReturn(actionNameToMediaTypesMapper_2);
        when(actionNameToMediaTypesMapper_1.getActionNameToMediaTypesMap()).thenReturn(mediaTypeToSchemaIdMap_1);
        when(actionNameToMediaTypesMapper_2.getActionNameToMediaTypesMap()).thenReturn(mediaTypeToSchemaIdMap_2);

        defaultMediaTypesMappingCache.initialise();

        final Optional<MediaTypes> mediaTypes_1 = defaultMediaTypesMappingCache.mediaTypesFor(actionName_1);
        final Optional<MediaTypes> mediaTypes_2 = defaultMediaTypesMappingCache.mediaTypesFor(actionName_2);
        final Optional<MediaTypes> mediaTypes_3 = defaultMediaTypesMappingCache.mediaTypesFor(actionName_3);

        assertThat(mediaTypes_1.get().getRequestMediaType(), is(Optional.of(requestMediaType_1)));
        assertThat(mediaTypes_1.get().getResponseMediaType(), is(Optional.of(responseMediaType_1)));
        assertThat(mediaTypes_2.get().getRequestMediaType(), is(Optional.of(requestMediaType_2)));
        assertThat(mediaTypes_2.get().getResponseMediaType(), is(Optional.of(responseMediaType_2)));
        assertThat(mediaTypes_3.get().getRequestMediaType(), is(Optional.of(requestMediaType_3)));
        assertThat(mediaTypes_3.get().getResponseMediaType(), is(Optional.of(responseMediaType_3)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnOptionalEmptyForUnknownActionName() throws Exception {
        final String actionName = "example.add-recipe";

        final MediaType requestMediaType = new MediaType("application/vnd.example.add-recipe+json");
        final MediaType responseMediaType = new MediaType("application/vnd.example.recipe-added+json");

        final ImmutableMap<String, MediaTypes> mediaTypeToSchemaIdMap = of(actionName, new MediaTypes(requestMediaType, responseMediaType));

        final Bean<ActionNameToMediaTypesMapper> bean_1 = mock(Bean.class);
        final ActionNameToMediaTypesMapper actionNameToMediaTypesMapper = mock(ActionNameToMediaTypesMapper.class);

        when(schemaIdMappingObserver.getNameMediaTypesMappers()).thenReturn(singletonList(bean_1));
        when(beanInstantiater.instantiate(bean_1)).thenReturn(actionNameToMediaTypesMapper);
        when(actionNameToMediaTypesMapper.getActionNameToMediaTypesMap()).thenReturn(mediaTypeToSchemaIdMap);

        defaultMediaTypesMappingCache.initialise();

        final Optional<MediaTypes> mediaTypes = defaultMediaTypesMappingCache.mediaTypesFor("application/vnd.example.unknown+json");

        assertThat(mediaTypes, is(Optional.empty()));
    }
}
