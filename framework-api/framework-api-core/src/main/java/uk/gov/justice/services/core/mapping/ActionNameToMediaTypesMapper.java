package uk.gov.justice.services.core.mapping;

import java.util.Map;

public interface ActionNameToMediaTypesMapper {

    Map<String, MediaTypes> getActionNameToMediaTypesMap();
}
