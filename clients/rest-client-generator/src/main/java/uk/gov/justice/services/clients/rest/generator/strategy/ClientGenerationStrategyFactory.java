package uk.gov.justice.services.clients.rest.generator.strategy;

import static uk.gov.justice.raml.common.config.GeneratorProperties.isActionMapping;

import uk.gov.justice.raml.core.GeneratorConfig;

public final class ClientGenerationStrategyFactory {

    private ClientGenerationStrategyFactory() {
    }

    /**
     * Factory for creating a {@link ClientGenerationStrategy} from the GeneratorConfig properties
     * Returns either a {@link ClientActionMappingGeneration} or {@link ClientMediaTypeGeneration}
     * strategy. This is set using the actionMapping generator property.
     *
     * actionMapping set to true returns - {@link ClientActionMappingGeneration}
     *
     * actionMapping set to false or not set returns -  {@link ClientMediaTypeGeneration}
     *
     * @param generatorConfig the generator configuration to check
     * @return the selected ClientGenerationStrategy
     */
    public static ClientGenerationStrategy createFrom(final GeneratorConfig generatorConfig) {
        if (isActionMapping(generatorConfig)) {
            return new ClientActionMappingGeneration();
        }
        return new ClientMediaTypeGeneration();
    }
}
