package uk.gov.justice.api.mapper;

import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypeToSchemaIdMapper;

import java.util.HashMap;
import java.util.Map;

public class TestMediaTypeToSchemaIdMapper implements MediaTypeToSchemaIdMapper  {

    private final Map<MediaType, String> mediaTypeToSchemaIds = new HashMap<>();

    public TestMediaTypeToSchemaIdMapper() {
        mediaTypeToSchemaIds.put(new MediaType("application/vnd.example.get-recipe+json"), "http://justice.gov.uk/schemas/test/example.get-recipe.json");
    }

    @Override
    public Map<MediaType, String> getMediaTypeToSchemaIdMap() {
        return mediaTypeToSchemaIds;
    }
}
