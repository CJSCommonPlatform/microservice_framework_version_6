package uk.gov.justice.services.generators.commons.mapping;

import static uk.gov.justice.services.generators.commons.helper.GeneratedClassWriter.writeClass;
import static uk.gov.justice.services.generators.commons.helper.Names.MAPPER_PACKAGE_NAME;
import static uk.gov.justice.services.generators.commons.helper.Names.packageNameOf;

import uk.gov.justice.subscription.domain.Event;
import uk.gov.justice.maven.generator.io.files.parser.core.GeneratorConfig;

import java.util.List;

import com.squareup.javapoet.TypeSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates MediaType to schema id mapping class for a given RAML object
 */
public class SubscriptionMediaTypeToSchemaIdGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionMediaTypeToSchemaIdGenerator.class);
    private final SubscriptionSchemaMappingClassNameGenerator subscriptionSchemaMappingClassNameGenerator = new SubscriptionSchemaMappingClassNameGenerator();
    private final SubscriptionMediaTypeToSchemaIdMapperClassBuilder subscriptionMediaTypeToSchemaIdMapperClassBuilder = new SubscriptionMediaTypeToSchemaIdMapperClassBuilder(subscriptionSchemaMappingClassNameGenerator);

    public void generateMediaTypeToSchemaIdMapper(
            final String contextName,
            final String componentName,
            final List<Event> events,
            final GeneratorConfig configuration) {

        final TypeSpec typeSpec = subscriptionMediaTypeToSchemaIdMapperClassBuilder
                .typeSpecWith(
                        contextName,
                        componentName,
                        events
                );

        writeClass(configuration, packageNameOf(configuration, MAPPER_PACKAGE_NAME), typeSpec, LOGGER);
    }
}
