package uk.gov.justice.services.clients.rest.generator.strategy;

import static uk.gov.justice.services.generators.commons.config.GeneratorProperties.isActionMapping;

import uk.gov.justice.raml.core.GeneratorConfig;

public final class ClientGenerationStrategyFactory {

    private static final ClientActionMappingGeneration CLIENT_ACTION_MAPPING_GENERATION = new ClientActionMappingGeneration();
    private static final ClientMediaTypeGeneration CLIENT_MEDIA_TYPE_GENERATION = new ClientMediaTypeGeneration();

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
            return CLIENT_ACTION_MAPPING_GENERATION;
        }
        return CLIENT_MEDIA_TYPE_GENERATION;
    }
}
