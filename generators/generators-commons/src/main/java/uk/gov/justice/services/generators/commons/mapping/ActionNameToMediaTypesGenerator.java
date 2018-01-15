package uk.gov.justice.services.generators.commons.mapping;

import static uk.gov.justice.services.generators.commons.helper.GeneratedClassWriter.writeClass;
import static uk.gov.justice.services.generators.commons.helper.Names.MAPPER_PACKAGE_NAME;
import static uk.gov.justice.services.generators.commons.helper.Names.packageNameOf;

import uk.gov.justice.raml.core.GeneratorConfig;

import com.squareup.javapoet.TypeSpec;
import org.raml.model.Raml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionNameToMediaTypesGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionNameToMediaTypesGenerator.class);
    private final ActionNameToMediaTypesParser actionNameToMediaTypesParser = new ActionNameToMediaTypesParser(new ActionMappingParser());
    private final SchemaMappingClassNameGenerator schemaMappingClassNameGenerator = new SchemaMappingClassNameGenerator();
    private final ActionNameToMediaTypesMapperClassBuilder nameToMediaTypesMapperClassBuilder = new ActionNameToMediaTypesMapperClassBuilder(schemaMappingClassNameGenerator);

    public void generateActionNameToMediaTypes(final Raml raml, final GeneratorConfig configuration) {

        final TypeSpec typeSpec = nameToMediaTypesMapperClassBuilder.generate(
                actionNameToMediaTypesParser.parseFrom(raml),
                raml.getBaseUri()
        );

        writeClass(configuration, packageNameOf(configuration, MAPPER_PACKAGE_NAME), typeSpec, LOGGER);
    }
}
