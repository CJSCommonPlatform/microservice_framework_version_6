package uk.gov.justice.services.adapters.rest.generator.strategy;

import static uk.gov.justice.raml.common.config.GeneratorProperties.isActionMapping;

import uk.gov.justice.raml.core.GeneratorConfig;

public final class AdapterGenerationStrategyFactory {

    private AdapterGenerationStrategyFactory() {
    }

    /**
     * Factory for creating a {@link AdapterGenerationStrategy} from the GeneratorConfig properties
     * Returns either a {@link AdapterActionMapperGeneration} or {@link AdapterMediaTypeGeneration}
     * strategy. This is set using the actionMapping generator property.
     *
     * actionMapping set to true returns - {@link AdapterActionMapperGeneration}
     *
     * actionMapping set to false or not set returns -  {@link AdapterMediaTypeGeneration}
     *
     * @param generatorConfig the generator configuration to check
     * @return the selected ClientGenerationStrategy
     */
    public static AdapterGenerationStrategy createFrom(final GeneratorConfig generatorConfig) {
        if (isActionMapping(generatorConfig)) {
            return new AdapterActionMapperGeneration();
        }
        return new AdapterMediaTypeGeneration();
    }
}
