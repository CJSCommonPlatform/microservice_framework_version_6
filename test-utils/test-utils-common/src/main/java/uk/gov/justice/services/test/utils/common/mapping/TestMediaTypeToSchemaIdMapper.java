package uk.gov.justice.services.test.utils.common.mapping;

import uk.gov.justice.services.core.mapping.MediaType;
import uk.gov.justice.services.core.mapping.MediaTypeToSchemaIdMapper;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Default;

@Default
public class TestMediaTypeToSchemaIdMapper implements MediaTypeToSchemaIdMapper {

    @Override
    public Map<MediaType, String> getMediaTypeToSchemaIdMap() {
        return new HashMap<>();
    }
}
