package uk.gov.justice.services.core.json;

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypeToSchemaIdMapper;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class SchemaIdMappingCacheMockTest {

    private final SchemaIdMappingCacheMock schemaIdMappingCacheMock = new SchemaIdMappingCacheMock();

    @Test
    public void shouldFindAllMapperClassesOnTheClasspathAndPutInTheCache() throws Exception {

        schemaIdMappingCacheMock.initialize(getClass().getPackage().getName());

        assertThat(schemaIdMappingCacheMock.schemaIdFor(new MediaType("application/vnd.media-type-a+json")),
                is(of("http://justice.gov.uk/context/schema_a.json")));
        assertThat(schemaIdMappingCacheMock.schemaIdFor(new MediaType("application/vnd.media-type-b+json")),
                is(of("http://justice.gov.uk/context/schema_b.json")));
        assertThat(schemaIdMappingCacheMock.schemaIdFor(new MediaType("application/vnd.media-type-c+json")),
                is(of("http://justice.gov.uk/context/schema_c.json")));
        assertThat(schemaIdMappingCacheMock.schemaIdFor(new MediaType("application/vnd.media-type-d+json")),
                is(of("http://justice.gov.uk/context/schema_d.json")));
    }
}

@SuppressWarnings("unused")
class MediaTypeToSchemaIdMapper_1 implements MediaTypeToSchemaIdMapper {

    private final Map<MediaType, String> mediaTypeToSchemaIds = new HashMap<>();

    MediaTypeToSchemaIdMapper_1() {
        mediaTypeToSchemaIds.put(new MediaType("application/vnd.media-type-a+json"), "http://justice.gov.uk/context/schema_a.json");
        mediaTypeToSchemaIds.put(new MediaType("application/vnd.media-type-b+json"), "http://justice.gov.uk/context/schema_b.json");
    }

    @Override
    public Map<MediaType, String> getMediaTypeToSchemaIdMap() {
        return mediaTypeToSchemaIds;
    }
}

@SuppressWarnings("unused")
class MediaTypeToSchemaIdMapper_2 implements MediaTypeToSchemaIdMapper {

    private final Map<MediaType, String> mediaTypeToSchemaIds = new HashMap<>();

    MediaTypeToSchemaIdMapper_2() {
        mediaTypeToSchemaIds.put(new MediaType("application/vnd.media-type-c+json"), "http://justice.gov.uk/context/schema_c.json");
        mediaTypeToSchemaIds.put(new MediaType("application/vnd.media-type-d+json"), "http://justice.gov.uk/context/schema_d.json");
    }

    @Override
    public Map<MediaType, String> getMediaTypeToSchemaIdMap() {
        return mediaTypeToSchemaIds;
    }
}

