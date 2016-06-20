package uk.gov.justice.services.adapters.rest.generator;


import static uk.gov.justice.services.generators.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;

import uk.gov.justice.services.adapter.rest.processor.RestProcessor;
import uk.gov.justice.services.generators.test.utils.BaseGeneratorTest;

import java.util.Map;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseRestAdapterGeneratorTest extends BaseGeneratorTest {

    protected static final Map<String, String> ACTION_MAPPING_TRUE = generatorProperties().withActionMappingOf(true).build();
    protected static final Map<String, String> ACTION_MAPPING_FALSE = generatorProperties().withActionMappingOf(false).build();

    @Mock
    protected RestProcessor restProcessor;

    @Before
    public void before() {
        super.before();
        generator = new RestAdapterGenerator();
    }
}
