package uk.gov.justice.services.adapters.test.utils.config;

import java.util.HashMap;
import java.util.Map;

public class GeneratorPropertiesBuilder {

    private static final String ACTION_MAPPING_KEY = "actionMapping";
    private static final String SERVICE_COMPONENT_KEY = "serviceComponent";
    private final Map<String, String> properties = new HashMap<>();

    public static GeneratorPropertiesBuilder generatorProperties() {
        return new GeneratorPropertiesBuilder();
    }

    public GeneratorPropertiesBuilder withActionMappingOf(final Boolean actionMapping) {
        properties.put(ACTION_MAPPING_KEY, actionMapping.toString());
        return this;
    }

    public GeneratorPropertiesBuilder withServiceComponentOf(final String serviceComponent) {
        properties.put(SERVICE_COMPONENT_KEY, serviceComponent);
        return this;
    }

    public Map<String, String> build() {
        return properties;
    }
}
