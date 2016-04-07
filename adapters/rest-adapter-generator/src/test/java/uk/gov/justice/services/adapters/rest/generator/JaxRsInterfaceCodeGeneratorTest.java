package uk.gov.justice.services.adapters.rest.generator;

import com.sun.codemodel.JCodeModel;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.raml.model.Raml;
import org.raml.model.Resource;
import uk.gov.justice.raml.core.GeneratorConfig;

import static java.nio.file.Paths.get;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.adapters.test.utils.builder.ActionBuilder.action;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;

/**
 * Unit tests for the {@link JaxRsInterfaceCodeGenerator} class.
 */
public class JaxRsInterfaceCodeGeneratorTest {

    private static final String BASE_PACKAGE = "org.raml.test";

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();


    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfInterfaceIsAddedTwice() throws Exception {
        JCodeModel codeModel = new JCodeModel();

        Raml raml = restRamlWithDefaults().with(
                resource("/some/path")
                        .with(action(POST).withDefaultRequestType())
        ).build();
        Resource resource = raml.getResources().values().iterator().next();

        GeneratorConfig config = new GeneratorConfig(
                get(outputFolder.getRoot().getAbsolutePath()),
                get(outputFolder.getRoot().getAbsolutePath()),
                BASE_PACKAGE);

        JaxRsInterfaceCodeGenerator generator = new JaxRsInterfaceCodeGenerator(codeModel, config);
        generator.createInterface(resource);

        generator = new JaxRsInterfaceCodeGenerator(codeModel, config);
        generator.createInterface(resource);
    }
}
