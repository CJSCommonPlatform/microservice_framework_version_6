package uk.gov.justice.services.adapters.rest.generator;


import static uk.gov.justice.services.adapters.test.utils.config.GeneratorPropertiesBuilder.generatorProperties;

import uk.gov.justice.services.adapter.rest.RestProcessor;
import uk.gov.justice.services.adapters.test.utils.compiler.JavaCompilerUtil;

import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseRestAdapterGeneratorTest {

    protected static final String BASE_PACKAGE = "org.raml.test";
    protected static final Map<String, String> ACTION_MAPPING_TRUE = generatorProperties().withActionMappingOf(true).build();
    protected static final Map<String, String> ACTION_MAPPING_FALSE = generatorProperties().withActionMappingOf(false).build();

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected JavaCompilerUtil compiler;
    protected RestAdapterGenerator generator;


    @Mock
    protected RestProcessor restProcessor;

    @Before
    public void before() {
        generator = new RestAdapterGenerator();
        compiler = new JavaCompilerUtil(outputFolder.getRoot(), outputFolder.getRoot());
    }
}
