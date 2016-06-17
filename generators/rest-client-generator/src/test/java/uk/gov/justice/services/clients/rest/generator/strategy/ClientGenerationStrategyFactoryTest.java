package uk.gov.justice.services.clients.rest.generator.strategy;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.emptyPathConfigurationWith;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;

import uk.gov.justice.raml.core.GeneratorConfig;

import org.junit.Test;

public class ClientGenerationStrategyFactoryTest {

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(ClientGenerationStrategyFactory.class);
    }

    @Test
    public void shouldReturnActionMappingGenerationStrategy() throws Exception {
        GeneratorConfig generatorConfig = emptyPathConfigurationWith(generatorProperties()
                .withActionMappingOf(true)
                .build());

        ClientGenerationStrategy generationStrategy = ClientGenerationStrategyFactory.createFrom(generatorConfig);
        assertThat(generationStrategy, instanceOf(ClientActionMappingGeneration.class));
    }

    @Test
    public void shouldReturnMediaTypeGenerationStrategy() throws Exception {
        GeneratorConfig generatorConfig = emptyPathConfigurationWith(generatorProperties()
                .withActionMappingOf(false)
                .build());

        ClientGenerationStrategy generationStrategy = ClientGenerationStrategyFactory.createFrom(generatorConfig);
        assertThat(generationStrategy, instanceOf(ClientMediaTypeGeneration.class));
    }

    @Test
    public void shouldReturnMediaTypeGenerationStrategyIfNotSet() throws Exception {
        GeneratorConfig generatorConfig = emptyPathConfigurationWith(null);

        ClientGenerationStrategy generationStrategy = ClientGenerationStrategyFactory.createFrom(generatorConfig);
        assertThat(generationStrategy, instanceOf(ClientMediaTypeGeneration.class));
    }
}