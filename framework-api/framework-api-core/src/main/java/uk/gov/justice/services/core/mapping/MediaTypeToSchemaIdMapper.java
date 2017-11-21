package uk.gov.justice.services.core.mapping;

import java.util.Map;

public interface MediaTypeToSchemaIdMapper {

    Map<MediaType, String> getMediaTypeToSchemaIdMap();
}
