package uk.gov.justice.services.adapters.rest.generator;

import static org.apache.commons.lang.Validate.notNull;

import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.core.annotation.Component;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.raml.model.Raml;
import org.raml.model.Resource;

/**
 * Code generator for generating JAX-RS classes based on RAML resources.
 */
class JaxRsCodeGenerator {

    private final GeneratorConfig config;
    private final JCodeModel codeModel;
    private JaxRsInterfaceCodeGenerator interfaceGenerator;
    private JaxRsImplementationCodeGenerator implementationGenerator;
    private JaxRsApplicationCodeGenerator applicationGenerator;

    JaxRsCodeGenerator(final GeneratorConfig config) {
        notNull(config, "Configuration can't be null");
        this.config = config;
        codeModel = new JCodeModel();
        interfaceGenerator = new JaxRsInterfaceCodeGenerator(codeModel, config);
        implementationGenerator = new JaxRsImplementationCodeGenerator(codeModel);
        applicationGenerator = new JaxRsApplicationCodeGenerator(codeModel, config);
    }

    /**
     * Create an interface from a RAML resource.
     *
     * @param resource the RAML resource definition
     * @return the fully qualified name of the interface created
     */
    String createInterface(final Resource resource) {
        return interfaceGenerator.createInterface(resource).fullName();
    }

    /**
     * Create an implementation class from a RAML resource.
     *
     * NB: this requires the interface to have been created already.
     *
     * @param interfaceName the fully qualified name of the interface to base this implementation
     *                      on
     * @param component     the Component to use for the Adaptor annotation
     * @return the fully qualified name of the class created
     */
    String createImplementation(final String interfaceName, final Component component) {
        JDefinedClass resourceInterface = codeModel._getClass(interfaceName);
        return implementationGenerator.createImplementation(resourceInterface, component).fullName();
    }

    /**
     * Create an application class from a RAML definition.
     *
     * @param raml                the RAML definition
     * @param implementationNames a collection of class names for the resources this application
     *                            should use
     * @return the fully qualified name of the application class created.
     */
    String createApplication(final Raml raml, final Collection<String> implementationNames) {
        return applicationGenerator.createApplication(raml, implementationNames).fullName();
    }

    /**
     * Generate the class files from the code model.
     */
    void generate() {
        try (final PrintStream printStream = new PrintStream(new ByteArrayOutputStream())) {
            codeModel.build(config.getOutputDirectory().toFile(), printStream);
        } catch (IOException ex) {
            throw new RuntimeException("Could not write generated classes", ex);
        }
    }

}
