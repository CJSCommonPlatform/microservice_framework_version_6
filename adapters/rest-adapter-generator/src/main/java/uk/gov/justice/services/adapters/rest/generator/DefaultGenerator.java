package uk.gov.justice.services.adapters.rest.generator;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import org.apache.commons.lang.Validate;
import org.raml.model.Raml;
import org.raml.model.Resource;
import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.core.GeneratorConfig;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class DefaultGenerator implements Generator {

    @Override
    public void run(final Raml raml, final GeneratorConfig configuration) {
        validate(configuration);

        Collection<Resource> ramlResourceModels = raml.getResources().values();
        try {
            JaxRsResourceCodeGenerator resourceCode = new JaxRsResourceCodeGenerator(configuration);
            for (final Resource ramlResourceModel : ramlResourceModels) {
                JDefinedClass resourceInterface = resourceCode.createInterface(ramlResourceModel);

                    resourceCode.createImplementation(resourceInterface);
            }
            resourceCode.generate();
        } catch (JClassAlreadyExistsException | IOException e) {
            throw new RuntimeException("Could not generate classes from RAML", e);
        }
    }

    private void validate(final GeneratorConfig configuration) {
        Validate.notNull(configuration, "configuration can't be null");

        final File outputDirectory = configuration.getOutputDirectory().toFile();
        Validate.notNull(outputDirectory, "outputDirectory can't be null");

        Validate.isTrue(outputDirectory.isDirectory(), outputDirectory
                + " is not a pre-existing directory");
        Validate.isTrue(outputDirectory.canWrite(), outputDirectory
                + " can't be written to");

        Validate.notEmpty(configuration.getBasePackageName(),
                "base package name can't be empty");
    }

}
