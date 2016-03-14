package uk.gov.justice.services.adapters.rest.generator;

import com.sun.codemodel.JDefinedClass;
import org.raml.model.Raml;
import org.raml.model.Resource;
import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.core.GeneratorConfig;

import java.io.File;
import java.util.Collection;

import static java.lang.String.format;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

public class RestAdapterGenerator implements Generator {

    @Override
    public void run(final Raml raml, final GeneratorConfig configuration) {
        validate(configuration);

        Collection<Resource> resources = raml.getResources().values();

        JaxRsCodeGenerator codeGenerator = new JaxRsCodeGenerator(configuration);
        for (final Resource resource : resources) {
            JDefinedClass resourceInterface = codeGenerator.createInterface(resource);
            codeGenerator.createImplementation(resourceInterface);
        }
        codeGenerator.generate();
    }

    private void validate(final GeneratorConfig configuration) {
        notNull(configuration, "Configuration can't be null");
        notEmpty(configuration.getBasePackageName(), "Base package name can't be empty");

        final File outputDirectory = configuration.getOutputDirectory().toFile();
        notNull(outputDirectory, "OutputDirectory can't be null");
        isTrue(outputDirectory.isDirectory(), format("%s is not a pre-existing directory", outputDirectory));
        isTrue(outputDirectory.canWrite(), format("%s can't be written to", outputDirectory));
    }

}
