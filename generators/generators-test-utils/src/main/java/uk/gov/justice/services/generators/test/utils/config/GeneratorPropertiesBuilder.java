package uk.gov.justice.services.generators.test.utils.config;

import java.util.HashMap;
import java.util.Map;

public class GeneratorPropertiesBuilder {

    private static final String SERVICE_COMPONENT_KEY = "serviceComponent";
    private final Map<String, String> properties = new HashMap<>();

    public static GeneratorPropertiesBuilder generatorProperties() {
        return new GeneratorPropertiesBuilder();
    }


    public GeneratorPropertiesBuilder withServiceComponentOf(final String serviceComponent) {
        properties.put(SERVICE_COMPONENT_KEY, serviceComponent);
        return this;
    }

    public GeneratorPropertiesBuilder withDefaultServiceComponent() {
        properties.put(SERVICE_COMPONENT_KEY, "COMMAND_API");
        return this;
    }

    public Map<String, String> build() {
        return properties;
    }
}
