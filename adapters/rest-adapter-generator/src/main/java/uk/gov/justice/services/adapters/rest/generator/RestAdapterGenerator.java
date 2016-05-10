package uk.gov.justice.services.adapters.rest.generator;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;
import static uk.gov.justice.raml.common.generator.Names.JAVA_FILENAME_SUFFIX;
import static uk.gov.justice.raml.common.generator.Names.MAPPER_PACKAGE_NAME;
import static uk.gov.justice.raml.common.generator.Names.RESOURCE_PACKAGE_NAME;
import static uk.gov.justice.raml.common.generator.Names.packageNameOf;

import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.core.GeneratorConfig;
import uk.gov.justice.services.adapters.rest.generator.strategy.AdapterGenerationStrategy;
import uk.gov.justice.services.adapters.rest.generator.strategy.AdapterGenerationStrategyFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.raml.model.Raml;

public class RestAdapterGenerator implements Generator {

    @Override
    public void run(final Raml raml, final GeneratorConfig configuration) {
        final AdapterGenerationStrategy generationStrategy = AdapterGenerationStrategyFactory.createFrom(configuration);

        validate(configuration);
        generationStrategy.validator().validate(raml);

        final JaxRsInterfaceGenerator interfaceGenerator = new JaxRsInterfaceGenerator();
        final JaxRsImplementationGenerator implementationGenerator = new JaxRsImplementationGenerator(configuration);
        final JaxRsApplicationCodeGenerator applicationGenerator = new JaxRsApplicationCodeGenerator(configuration);

        writeToSubPackage(interfaceGenerator.generateFor(raml), configuration, RESOURCE_PACKAGE_NAME);
        final List<String> implementationNames = writeToSubPackage(
                implementationGenerator.generateFor(raml), configuration, RESOURCE_PACKAGE_NAME);

        writeToBasePackage(applicationGenerator.generateFor(raml, implementationNames), configuration);
        writeToSubPackage(generationStrategy.generateFor(raml), configuration, MAPPER_PACKAGE_NAME);
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
     * Write a list of classes to a specified package.
     *
     * @param typeSpecs      the list of typeSpecs to write to file
     * @param configuration  the configuration that provides the base package name
     * @param subPackageName the sub package to append to the base package
     * @return the list of class names written to file
     * @throws IllegalStateException if an IOException is thrown while writing to file
     */
    private List<String> writeToSubPackage(final List<TypeSpec> typeSpecs,
                                           final GeneratorConfig configuration,
                                           final String subPackageName) {
        return writeToPackage(typeSpecs, configuration, packageNameOf(configuration, subPackageName));
    }

    /**
     * Write class to the base package provided in the configuration
     *
     * @param typeSpec      the typeSpec to write to file
     * @param configuration the configuration that provides the base package name
     */
    private void writeToBasePackage(final TypeSpec typeSpec, final GeneratorConfig configuration) {
        writeToPackage(singletonList(typeSpec), configuration, configuration.getBasePackageName());
    }

    /**
     * Write a list of classes to a specified package.
     *
     * @param typeSpecs     the list of typeSpecs to write to file
     * @param configuration the configuration that provides the base package name
     * @param packageName   the package name to write the classes
     * @return the list of class names written to file
     * @throws IllegalStateException if an IOException is thrown while writing to file
     */
    private List<String> writeToPackage(final List<TypeSpec> typeSpecs,
                                        final GeneratorConfig configuration,
                                        final String packageName) {
        final List<String> implementationNames = new LinkedList<>();

        typeSpecs.stream()
                .forEach(typeSpec -> {
                    try {
                        if (classDoesNotExist(configuration, typeSpec)) {
                            JavaFile.builder(packageName, typeSpec)
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
        for (final Path path : configuration.getSourcePaths()) {

            final String pathname = format("%s/%s/%s", path.toString(),
                    getBasePackagePath(configuration), getResourcePath(typeSpec));

            if (new File(pathname).exists()) {
                return false;
            }
        }
        return true;
    }

    private String getResourcePath(final TypeSpec typeSpec) {
        return format("%s/%s%s", RESOURCE_PACKAGE_NAME, typeSpec.name, JAVA_FILENAME_SUFFIX);
    }

    private String getBasePackagePath(final GeneratorConfig configuration) {
        return configuration.getBasePackageName().replaceAll("\\.", "/");
    }

}
