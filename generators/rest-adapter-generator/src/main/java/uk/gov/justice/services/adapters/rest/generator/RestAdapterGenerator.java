package uk.gov.justice.services.adapters.rest.generator;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;
import static org.raml.model.ActionType.DELETE;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.PATCH;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.PUT;
import static uk.gov.justice.services.generators.commons.helper.GeneratedClassWriter.writeClass;
import static uk.gov.justice.services.generators.commons.helper.Names.MAPPER_PACKAGE_NAME;
import static uk.gov.justice.services.generators.commons.helper.Names.RESOURCE_PACKAGE_NAME;
import static uk.gov.justice.services.generators.commons.helper.Names.packageNameOf;

import uk.gov.justice.maven.generator.io.files.parser.core.Generator;
import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorConfig;
import uk.gov.justice.services.generators.commons.mapping.ActionNameToMediaTypesGenerator;
import uk.gov.justice.services.generators.commons.mapping.MediaTypeToSchemaIdGenerator;
import uk.gov.justice.services.generators.commons.validator.ActionMappingRamlValidator;
import uk.gov.justice.services.generators.commons.validator.CompositeRamlValidator;
import uk.gov.justice.services.generators.commons.validator.ContainsActionsRamlValidator;
import uk.gov.justice.services.generators.commons.validator.ContainsResourcesRamlValidator;
import uk.gov.justice.services.generators.commons.validator.MultipartHasFormParameters;
import uk.gov.justice.services.generators.commons.validator.RamlValidator;
import uk.gov.justice.services.generators.commons.validator.RequestContentTypeRamlValidator;
import uk.gov.justice.services.generators.commons.validator.ResponseContentTypeRamlValidator;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.squareup.javapoet.TypeSpec;
import org.raml.model.Raml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestAdapterGenerator implements Generator<Raml> {

    private Logger logger = LoggerFactory.getLogger(RestAdapterGenerator.class);

    private final RamlValidator validator = new CompositeRamlValidator(
            new ContainsResourcesRamlValidator(),
            new ContainsActionsRamlValidator(),
            new RequestContentTypeRamlValidator(DELETE, PATCH, POST, PUT),
            new ResponseContentTypeRamlValidator(GET),
            new ActionMappingRamlValidator(),
            new MultipartHasFormParameters()
    );

    @Override
    public void run(final Raml raml, final GeneratorConfig configuration) {

        validate(configuration);
        validator.validate(raml);

        final JaxRsInterfaceGenerator interfaceGenerator = new JaxRsInterfaceGenerator();
        final JaxRsImplementationGenerator implementationGenerator = new JaxRsImplementationGenerator(configuration);
        final JaxRsApplicationCodeGenerator applicationGenerator = new JaxRsApplicationCodeGenerator(configuration);
        final ActionMappingGenerator actionMappingGenerator = new ActionMappingGenerator();
        final MediaTypeToSchemaIdGenerator mediaTypeToSchemaIdGenerator = new MediaTypeToSchemaIdGenerator();
        final ActionNameToMediaTypesGenerator actionNameToMediaTypesGenerator = new ActionNameToMediaTypesGenerator();

        writeToSubPackage(interfaceGenerator.generateFor(raml), configuration, RESOURCE_PACKAGE_NAME);
        final List<String> implementationNames = writeToSubPackage(
                implementationGenerator.generateFor(raml), configuration, RESOURCE_PACKAGE_NAME);

        writeToBasePackage(applicationGenerator.generateFor(raml, implementationNames), configuration);
        writeToSubPackage(actionMappingGenerator.generateFor(raml), configuration, MAPPER_PACKAGE_NAME);

        mediaTypeToSchemaIdGenerator.generateMediaTypeToSchemaIdMapper(raml, configuration);
        actionNameToMediaTypesGenerator.generateActionNameToMediaTypes(raml, configuration);
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

        typeSpecs.forEach(typeSpec -> {
            writeClass(configuration, packageName, typeSpec, logger);
            implementationNames.add(typeSpec.name);
        });

        return implementationNames;
    }

}
