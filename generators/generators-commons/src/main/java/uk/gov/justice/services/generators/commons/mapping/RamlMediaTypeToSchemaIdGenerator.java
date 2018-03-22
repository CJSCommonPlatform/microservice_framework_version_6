package uk.gov.justice.services.generators.commons.mapping;

import static uk.gov.justice.services.generators.commons.helper.GeneratedClassWriter.writeClass;
import static uk.gov.justice.services.generators.commons.helper.Names.MAPPER_PACKAGE_NAME;
import static uk.gov.justice.services.generators.commons.helper.Names.packageNameOf;

import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorConfig;

import com.squareup.javapoet.TypeSpec;
import org.raml.model.Raml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates MediaType to schema id mapping class for a given RAML object
 */
public class RamlMediaTypeToSchemaIdGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RamlMediaTypeToSchemaIdGenerator.class);
    private final RamlSchemaMappingClassNameGenerator ramlSchemaMappingClassNameGenerator = new RamlSchemaMappingClassNameGenerator();
    private final MediaTypeToSchemaIdParser mediaTypeToSchemaIdParser = new MediaTypeToSchemaIdParser(new SchemaIdParser());
    private final RamlMediaTypeToSchemaIdMapperClassBuilder ramlMediaTypeToSchemaIdMapperClassBuilder = new RamlMediaTypeToSchemaIdMapperClassBuilder(ramlSchemaMappingClassNameGenerator);

    public void generateMediaTypeToSchemaIdMapper(final Raml raml, final GeneratorConfig configuration) {

        final TypeSpec typeSpec = ramlMediaTypeToSchemaIdMapperClassBuilder
                .typeSpecWith(
                        raml.getBaseUri(),
                        mediaTypeToSchemaIdParser.parseFrom(raml)
                );

        writeClass(configuration, packageNameOf(configuration, MAPPER_PACKAGE_NAME), typeSpec, LOGGER);
    }
}
