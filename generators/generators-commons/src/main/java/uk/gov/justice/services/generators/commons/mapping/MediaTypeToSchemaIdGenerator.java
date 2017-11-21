package uk.gov.justice.services.generators.commons.mapping;

import static uk.gov.justice.services.generators.commons.helper.GeneratedClassWriter.writeClass;
import static uk.gov.justice.services.generators.commons.helper.Names.MAPPER_PACKAGE_NAME;
import static uk.gov.justice.services.generators.commons.helper.Names.packageNameOf;

import uk.gov.justice.raml.core.GeneratorConfig;

import com.squareup.javapoet.TypeSpec;
import org.raml.model.Raml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates MediaType to schema id mapping class for a given RAML object
 */
public class MediaTypeToSchemaIdGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaTypeToSchemaIdGenerator.class);
    private final SchemaMappingClassNameGenerator schemaMappingClassNameGenerator = new SchemaMappingClassNameGenerator();
    private final MediaTypeToSchemaIdParser mediaTypeToSchemaIdParser = new MediaTypeToSchemaIdParser(new SchemaIdParser());
    private final MediaTypeToSchemaIdMapperClassBuilder mediaTypeToSchemaIdMapperClassBuilder = new MediaTypeToSchemaIdMapperClassBuilder(schemaMappingClassNameGenerator);

    public void generateMediaTypeToSchemaIdMapper(final Raml raml, final GeneratorConfig configuration) {

        final TypeSpec typeSpec = mediaTypeToSchemaIdMapperClassBuilder
                .typeSpecWith(
                        raml.getBaseUri(),
                        mediaTypeToSchemaIdParser.parseFrom(raml)
                );

        writeClass(configuration, packageNameOf(configuration, MAPPER_PACKAGE_NAME), typeSpec, LOGGER);
    }
}
