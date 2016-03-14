package uk.gov.justice.services.adapters.rest.generator;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.raml.model.Resource;
import uk.gov.justice.raml.core.GeneratorConfig;

import java.io.IOException;
import java.io.PrintStream;

import static org.apache.commons.lang.Validate.*;

public class JaxRsCodeGenerator {
    private final GeneratorConfig configuration;
    private final JCodeModel codeModel;
    private JaxRsInterfaceCodeGenerator interfaceGenerator;
    private JaxRsImplementationCodeGenerator implementationGenerator;

    public JaxRsCodeGenerator(final GeneratorConfig configuration) {
        notNull(configuration, "Configuration can't be null");
        this.configuration = configuration;
        codeModel = new JCodeModel();
        interfaceGenerator = new JaxRsInterfaceCodeGenerator(codeModel, configuration);
        implementationGenerator = new JaxRsImplementationCodeGenerator(codeModel);
    }

    public JDefinedClass createInterface(final Resource ramlResourceDef) {
        return interfaceGenerator.createInterface(ramlResourceDef);
    }

    public void createImplementation(JDefinedClass resourceInterface) {
        implementationGenerator.createImplementation(resourceInterface);
    }

    public void generate() {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final PrintStream ps = new PrintStream(baos);
            codeModel.build(configuration.getOutputDirectory().toFile(), ps);
            ps.close();
        } catch (IOException ex) {
            throw new RuntimeException("Could not write generated classes", ex);
        }
    }

}
