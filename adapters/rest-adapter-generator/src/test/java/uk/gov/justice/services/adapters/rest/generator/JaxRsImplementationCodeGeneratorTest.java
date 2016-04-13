package uk.gov.justice.services.adapters.rest.generator;

import uk.gov.justice.services.core.annotation.Component;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import org.junit.Test;

/**
 * Unit tests for the {@link JaxRsImplementationCodeGenerator} class.
 */
public class JaxRsImplementationCodeGeneratorTest {

    private static final String BASE_PACKAGE = "org.raml.test";

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfClassIsAddedTwice() throws Exception {
        JCodeModel codeModel = new JCodeModel();
        JDefinedClass definedInterface = codeModel._package(BASE_PACKAGE)._interface("TestInterface");

        JaxRsImplementationCodeGenerator generator = new JaxRsImplementationCodeGenerator(codeModel);

        generator.createImplementation(definedInterface, Component.COMMAND_API);
        generator.createImplementation(definedInterface, Component.COMMAND_API);
    }
}
