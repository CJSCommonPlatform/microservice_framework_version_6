package uk.gov.justice.services.adapters.rest.generator;

import static java.lang.String.format;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;
import static uk.gov.justice.services.adapters.rest.generator.Generators.componentFromBaseUriIn;
import static uk.gov.justice.services.adapters.rest.generator.Names.JAVA_FILENAME_SUFFIX;
import static uk.gov.justice.services.adapters.rest.generator.Names.RESOURCE_PACKAGE_NAME;
import static uk.gov.justice.services.adapters.rest.generator.Names.RESOURCE_PACKAGE_NAME_WITH_DOT;

import uk.gov.justice.raml.common.validator.CompositeRamlValidator;
import uk.gov.justice.raml.common.validator.ContainsActionsRamlValidator;
import uk.gov.justice.raml.common.validator.ContainsResourcesRamlValidator;
import uk.gov.justice.raml.common.validator.RamlValidator;
import uk.gov.justice.raml.common.validator.RequestContentTypeRamlValidator;
import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.adapters.rest.validator.BaseUriRamlValidator;
import uk.gov.justice.services.adapters.rest.validator.ResponseContentTypeRamlValidator;
import uk.gov.justice.services.core.annotation.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.raml.model.Raml;
import org.raml.model.Resource;

public class RestAdapterGenerator implements Generator {

    private static final String JAVA_SRC_PATH = "\\main\\java\\";

    private final RamlValidator validator = new CompositeRamlValidator(
            new ContainsResourcesRamlValidator(),
            new ContainsActionsRamlValidator(),
            new RequestContentTypeRamlValidator(),
            new ResponseContentTypeRamlValidator(),
            new BaseUriRamlValidator()
    );

    @Override
    public void run(final Raml raml, final GeneratorConfig configuration) {
        validate(configuration);
        validator.validate(raml);

        final Collection<Resource> resources = raml.getResources().values();
        final Component component = componentFromBaseUriIn(raml);

        final JaxRsInterfaceGenerator interfaceGenerator = new JaxRsInterfaceGenerator();
        final JaxRsImplementationGenerator implementationGenerator = new JaxRsImplementationGenerator(configuration);
        final JaxRsApplicationCodeGenerator applicationGenerator = new JaxRsApplicationCodeGenerator(configuration);

        writeToResourcePackage(interfaceGenerator.generateFor(resources), configuration);
        final List<String> implementationNames = writeToResourcePackage(implementationGenerator.generateFor(resources, component), configuration);

        writeToBasePackage(applicationGenerator.createApplication(implementationNames, raml), configuration);
    }

    private void validate(final GeneratorConfig configuration) {
        notNull(configuration, "Configuration can't be null");
        notEmpty(configuration.getBasePackageName(), "Base package name can't be empty");

        final File outputDirectory = configuration.getOutputDirectory().toFile();
        notNull(outputDirectory, "OutputDirectory can't be null");
        isTrue(outputDirectory.isDirectory(), format("%s is not a pre-existing directory", outputDirectory));
        isTrue(outputDirectory.canWrite(), format("%s can't be written to", outputDirectory));
    }

    /**
     * Write class to the base package provided in the configuration
     *
     * @param typeSpec      the typeSpec to write to file
     * @param configuration the configuration that provides the base package name
     */
    private void writeToBasePackage(final TypeSpec typeSpec, final GeneratorConfig configuration) {
        writeToBasePackage(Collections.singletonList(typeSpec), configuration, "");
    }

    /**
     * Write a list of classes to the resource package.
     *
     * @param typeSpecs     the list of typeSpecs to write to file
     * @param configuration the configuration that provides the base package name
     * @return the list of class names written to file
     */
    private List<String> writeToResourcePackage(final List<TypeSpec> typeSpecs, final GeneratorConfig configuration) {
        return writeToBasePackage(typeSpecs, configuration, RESOURCE_PACKAGE_NAME_WITH_DOT);
    }

    /**
     * Write a list of classes to a specified package.
     *
     * @param typeSpecs     the list of typeSpecs to write to file
     * @param configuration the configuration that provides the base package name
     * @return the list of class names written to file
     * @throws IllegalStateException if an IOException is thrown while writing to file
     */
    private List<String> writeToBasePackage(final List<TypeSpec> typeSpecs,
                                            final GeneratorConfig configuration,
                                            final String packageName) {

        final List<String> implementationNames = new ArrayList<>();

        typeSpecs.stream()
                .forEach(typeSpec -> {
                    try {
                        if (classDoesNotExist(configuration, typeSpec)) {
                            JavaFile.builder(configuration.getBasePackageName() + packageName, typeSpec)
                                    .build()
                                    .writeTo(configuration.getOutputDirectory());
                        }

                        implementationNames.add(typeSpec.name);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });

        return implementationNames;
    }

    private boolean classDoesNotExist(final GeneratorConfig configuration, final TypeSpec typeSpec) {
        final String relativeJavaSourcePath = configuration.getSourceDirectory().getParent().toString() + JAVA_SRC_PATH;
        final String basePackagePath = configuration.getBasePackageName().replaceAll("\\.", "\\\\");

        final String pathname = relativeJavaSourcePath + basePackagePath + "\\"
                + RESOURCE_PACKAGE_NAME + "\\"
                + typeSpec.name + JAVA_FILENAME_SUFFIX;

        return !new File(pathname).exists();
    }

}
