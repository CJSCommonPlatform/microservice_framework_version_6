package uk.gov.justice.services.core.mapping;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSchemaIdMappingCacheTest {

    @Mock
    private SchemaIdMappingCacheInitialiser schemaIdMappingCacheInitialiser;

    @InjectMocks
    private DefaultSchemaIdMappingCache defaultSchemaIdMappingCache;

    @Test
    public void shouldLazyInitialiseTheCache() throws Exception {

        final MediaType mediaType = new MediaType("application/vnd.example.add-recipe+json");
        final String schemaId = "http://justice.gov.uk/example/command/api/example.add-recipe.json";

        final Map<MediaType, String> mediaTypeToSchemaIdMap = of(mediaType, schemaId);

        when(schemaIdMappingCacheInitialiser.initialiseCache()).thenReturn(mediaTypeToSchemaIdMap);

        final Optional<String> schemaIdOptional = defaultSchemaIdMappingCache.schemaIdFor(mediaType);

        assertThat(schemaIdOptional.isPresent(), is(true));

        assertThat(schemaIdOptional.get(), is(schemaId));
    }

    @Test
    public void shouldInitialiseTheCacheOnlyOnce() throws Exception {

        final MediaType mediaType = new MediaType("application/vnd.example.add-recipe+json");
        final String schemaId = "http://justice.gov.uk/example/command/api/example.add-recipe.json";

        final Map<MediaType, String> mediaTypeToSchemaIdMap = of(mediaType, schemaId);

        when(schemaIdMappingCacheInitialiser.initialiseCache()).thenReturn(mediaTypeToSchemaIdMap);

        assertThat(defaultSchemaIdMappingCache.schemaIdFor(mediaType).get(), is(schemaId));
        assertThat(defaultSchemaIdMappingCache.schemaIdFor(mediaType).get(), is(schemaId));
        assertThat(defaultSchemaIdMappingCache.schemaIdFor(mediaType).get(), is(schemaId));
        assertThat(defaultSchemaIdMappingCache.schemaIdFor(mediaType).get(), is(schemaId));
        assertThat(defaultSchemaIdMappingCache.schemaIdFor(mediaType).get(), is(schemaId));
        assertThat(defaultSchemaIdMappingCache.schemaIdFor(mediaType).get(), is(schemaId));

        verify(schemaIdMappingCacheInitialiser, times(1)).initialiseCache();
    }

    @Test
    public void shouldReturnEmptyIfNoMediaTypeFoundInCache() throws Exception {

        final MediaType mediaType = new MediaType("application/vnd.example.add-recipe+json");

        when(schemaIdMappingCacheInitialiser.initialiseCache()).thenReturn(new HashMap<>());

        final Optional<String> schemaIdOptional = defaultSchemaIdMappingCache.schemaIdFor(mediaType);

        assertThat(schemaIdOptional.isPresent(), is(false));
    }
}
