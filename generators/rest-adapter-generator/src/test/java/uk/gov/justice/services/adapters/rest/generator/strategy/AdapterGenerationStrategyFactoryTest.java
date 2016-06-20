package uk.gov.justice.services.adapters.rest.generator.strategy;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorConfigUtil.emptyPathConfigurationWith;
import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;

import uk.gov.justice.raml.core.GeneratorConfig;

import org.junit.Test;

public class AdapterGenerationStrategyFactoryTest {

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(AdapterGenerationStrategyFactory.class);
    }

    @Test
    public void shouldReturnActionMappingGenerationStrategy() throws Exception {
        GeneratorConfig generatorConfig = emptyPathConfigurationWith(generatorProperties()
                .withActionMappingOf(true)
                .build());

        AdapterGenerationStrategy generationStrategy = AdapterGenerationStrategyFactory.createFrom(generatorConfig);
        assertThat(generationStrategy, instanceOf(AdapterActionMapperGeneration.class));
    }

    @Test
    public void shouldReturnMediaTypeGenerationStrategy() throws Exception {
        GeneratorConfig generatorConfig = emptyPathConfigurationWith(generatorProperties()
                .withActionMappingOf(false)
                .build());

        AdapterGenerationStrategy generationStrategy = AdapterGenerationStrategyFactory.createFrom(generatorConfig);
        assertThat(generationStrategy, instanceOf(AdapterMediaTypeGeneration.class));
    }

    @Test
    public void shouldReturnMediaTypeGenerationStrategyIfNotSet() throws Exception {
        GeneratorConfig generatorConfig = emptyPathConfigurationWith(null);

        AdapterGenerationStrategy generationStrategy = AdapterGenerationStrategyFactory.createFrom(generatorConfig);
        assertThat(generationStrategy, instanceOf(AdapterMediaTypeGeneration.class));
    }

}